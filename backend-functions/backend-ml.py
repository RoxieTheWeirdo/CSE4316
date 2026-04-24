from flask import Flask, request, jsonify
import torch
import torch.nn as nn
import pickle
import numpy as np
import os
import random
from difflib import SequenceMatcher

app = Flask(__name__)

# Must match the architecture saved in the Colab notebook exactly,
# otherwise the weights won't load correctly.
class MealPlannerNN(nn.Module):
    def __init__(self, input_dim, output_dims):
        super().__init__()
        self.trunk = nn.Sequential(
            nn.Linear(input_dim, 256),
            nn.BatchNorm1d(256),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(256, 128),
            nn.BatchNorm1d(128),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(128, 64),
            nn.ReLU(),
        )
        self.heads = nn.ModuleDict({
            slot: nn.Linear(64, n_classes)
            for slot, n_classes in output_dims.items()
        })

    def forward(self, x):
        shared = self.trunk(x)
        return {slot: head(shared) for slot, head in self.heads.items()}

# Load everything once at startup — loading a PyTorch model takes ~2s
# and we don't want that cost on every request.
print('Loading model and encoders...')

with open('meal_planner_encoders.pkl', 'rb') as f:
    bundle = pickle.load(f)

label_encoders = bundle['label_encoders']
cat_encoders = bundle['cat_encoders']
num_scaler = bundle['num_scaler']
NUM_FEATURES = bundle['num_features']
CAT_FEATURES = bundle['cat_features']
MEAL_SLOTS = bundle['meal_slots']
OUTPUT_DIMS = bundle['output_dims']
INPUT_DIM = bundle['input_dim']

with open('meal_nutrition_lookup.pkl', 'rb') as f:
    meal_nutrition_lookup = pickle.load(f)

with open('slot_cal_lookup.pkl', 'rb') as f:
    slot_cal_lookup = pickle.load(f)

# Run on CPU — servers rarely have GPUs and it's fast enough for inference
DEVICE = torch.device('cpu')

model = MealPlannerNN(INPUT_DIM, OUTPUT_DIMS)
model.load_state_dict(
    torch.load('meal_planner_nn.pt', map_location=DEVICE)
)

# eval() disables Dropout and BatchNorm's training behavior —
# skipping this makes predictions inconsistent for the same input
model.eval()

print(f'✓ Model loaded — {INPUT_DIM} input features')
print(f'✓ Meal slots: {MEAL_SLOTS}')
print(f'✓ Nutrition lookup: {len(meal_nutrition_lookup)} meals')

def encode_user(user_data):
    """
    Convert a user profile dict into a float array for the model.
    Must match the exact preprocessing order from training (Colab Cell 4).
    """
    num_vals = np.array(
        [[float(user_data.get(f, 0)) for f in NUM_FEATURES]],
        dtype=np.float32
    )
    num_vals = num_scaler.transform(num_vals)

    cat_vals = []
    for col in CAT_FEATURES:
        val = str(user_data.get(col, 'unknown')).lower().strip()
        le = cat_encoders[col]

        # Unknown values (e.g. a new activity level) fall back to the
        # first known class instead of crashing
        if val not in le.classes_:
            print(f' Warning: unknown value "{val}" for "{col}" '
                  f'— using default "{le.classes_[0]}"')
            val = le.classes_[0]

        cat_vals.append([[le.transform([val])[0]]])

    if cat_vals:
        cat_arr = np.hstack(
            [np.array(c) for c in cat_vals]
        ).astype(np.float32)
        return np.hstack([num_vals, cat_arr])

    return num_vals

def predict_one_day(user_tensor, exclude=None, top_k=12,
                    temperature=1.2, cal_targets=None,
                    cal_tolerance=0.35):
    """
    Run the model for one day and return one meal per slot.
    exclude : meals to skip for variety across days
    top_k : sample from this many top candidates
    temperature : higher = more variety, lower = more repetitive
    cal_targets : {slot: target_calories}
    cal_tolerance: acceptable ± fraction of the calorie target
    """
    if exclude is None:
        exclude = set()

    if cal_targets is None:
        cal_targets = {}

    predictions = {}

    with torch.no_grad():
        logits = model(user_tensor)

        for slot in MEAL_SLOTS:
            slot_name = slot.replace('_meal', '')
            target = cal_targets.get(slot_name, 400)

            lo = target * (1 - cal_tolerance)
            hi = target * (1 + cal_tolerance)

            cal_map = slot_cal_lookup.get(slot_name, {})

            probs = torch.softmax(
                logits[slot] / temperature, dim=1
            )[0].cpu().numpy()

            classes = label_encoders[slot].classes_
            ranked = sorted(zip(probs, classes), reverse=True)

            valid = [
                (p, c) for p, c in ranked
                if c not in exclude
                and c in cal_map
                and lo <= cal_map[c] <= hi
            ]

            if valid:
                pool = valid[:top_k]
                pool_p = np.array([p for p, _ in pool])
                pool_p = pool_p / pool_p.sum()
                idx = np.random.choice(len(pool), p=pool_p)
                predictions[slot] = pool[idx][1]
            else:
                # Nothing fits the calorie window — pick the closest match
                top_any = [
                    (p, c) for p, c in ranked
                    if c not in exclude and c in cal_map
                ][:top_k]

                if top_any:
                    closest = min(
                        top_any,
                        key=lambda x: abs(cal_map.get(x[1], 0) - target)
                    )
                    predictions[slot] = closest[1]
                    print(f' Fallback for {slot_name}: {closest[1]}')
                else:
                    predictions[slot] = ranked[0][1]

    return predictions

def get_nutrition(meal_name, target_calories):
    """
    Look up nutrition for a meal and scale macros to hit target_calories.
    Grams are derived from the scaled total using standard densities
    (protein/carbs = 4 kcal/g, fat = 9 kcal/g), so macros stay
    internally consistent regardless of lookup data quality.
    """
    nutr = meal_nutrition_lookup.get(meal_name.lower().strip(), {})
    actual = nutr.get('calories', target_calories)

    if not actual or actual <= 0:
        actual = target_calories

    scaled_cal = round(min(target_calories / actual, 3.5) * actual, 1)

    protein_cal = nutr.get('protein', 0) * 4
    carbs_cal = nutr.get('carbs', 0) * 4
    fat_cal = nutr.get('fat', 0) * 9

    total_macro_cal = protein_cal + carbs_cal + fat_cal

    if total_macro_cal > 0:
        # Scale grams while preserving the original macro ratios
        protein_g = round(scaled_cal * (protein_cal / total_macro_cal) / 4, 1)
        carbs_g = round(scaled_cal * (carbs_cal / total_macro_cal) / 4, 1)
        fat_g = round(scaled_cal * (fat_cal / total_macro_cal) / 9, 1)
    else:
        # No macro data — fall back to a balanced 50/30/20 split
        protein_g = round(scaled_cal * 0.30 / 4, 1)
        carbs_g = round(scaled_cal * 0.50 / 4, 1)
        fat_g = round(scaled_cal * 0.20 / 9, 1)

    return {
        'calories': scaled_cal,
        'protein': protein_g,
        'carbs': carbs_g,
        'fat': fat_g,
    }

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'ok',
        'model': 'meal_planner_nn',
        'meals_in_lookup': len(meal_nutrition_lookup),
    })

@app.route('/generate-plan', methods=['POST'])
def generate_plan():
    try:
        data = request.get_json()

        if not data:
            return jsonify({
                'error': 'Request body is empty or not valid JSON'
            }), 400

        required_fields = [
            'age', 'gender', 'weight_kg', 'height_cm',
            'activity_level', 'dietary_preference', 'daily_calories'
        ]

        missing = [f for f in required_fields if f not in data]

        if missing:
            return jsonify({
                'error': 'Missing required fields',
                'missing': missing
            }), 400

        daily_calories = float(data['daily_calories'])
        days = int(data.get('days', 7))

        if daily_calories < 500 or daily_calories > 10000:
            return jsonify({
                'error': 'daily_calories must be between 500 and 10000'
            }), 400

        if days < 1 or days > 30:
            return jsonify({
                'error': 'days must be between 1 and 30'
            }), 400

        cal_targets = {
            'breakfast': daily_calories * 0.25,
            'lunch': daily_calories * 0.35,
            'dinner': daily_calories * 0.40,
            'snack': daily_calories * 0.10,
        }

        user_arr = encode_user(data)
        user_tensor = torch.tensor(
            user_arr, dtype=torch.float32
        ).to(DEVICE)

        plan = []
        recent = {slot: [] for slot in MEAL_SLOTS}

        EXCLUDE_WINDOW = 3

        for day in range(1, days + 1):
            exclude_today = set(
                meal
                for slot in MEAL_SLOTS
                for meal in recent[slot][-EXCLUDE_WINDOW:]
            )

            day_preds = predict_one_day(
                user_tensor,
                exclude=exclude_today,
                top_k=12,
                temperature=1.2,
                cal_targets=cal_targets,
                cal_tolerance=0.35,
            )

            for slot, meal in day_preds.items():
                slot_name = slot.replace('_meal', '')
                target_cal = cal_targets[slot_name]

                nutr = get_nutrition(meal, target_cal)

                plan.append({
                    'day': day,
                    'slot': slot_name,
                    'meal': meal.title(),
                    'calories': nutr['calories'],
                    'protein': nutr['protein'],
                    'carbs': nutr['carbs'],
                    'fat': nutr['fat'],
                })

                recent[slot].append(meal)

        return jsonify({
            'status': 'success',
            'user_id': data.get('user_id', 'unknown'),
            'daily_target': daily_calories,
            'plan': plan,
        })

    except Exception as e:
        print(f'Error in generate-plan: {str(e)}')
        return jsonify({
            'error': 'Internal server error',
            'details': str(e)
        }), 500

if __name__ == '__main__':
    app.run(
        host='0.0.0.0',
        port=int(os.environ.get('PORT', 5000)),
        debug=False
    )