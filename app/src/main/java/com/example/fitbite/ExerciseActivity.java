package com.example.fitbite;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExerciseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;

    private List<Exercise> todayList = new ArrayList<>();
    private List<Exercise> historyList = new ArrayList<>();
    private List<ExerciseItem> allExercisesList = new ArrayList<>();

    private EditText searchBar;
    private Button addExerciseBtn;

    private TextView tvTotalCalories, tvTotalTime;
    private FirebaseFirestore db;

    private String currentTab = "Today";
    private String currentSearchText = "";

    private TextView tvNoResults;

    private double userWeight = 70;
    private Set<String> expandedHeaders = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        bindViews();
        setupRecyclerView();
        setupTabs();
        setupFirebase();
        loadUserWeight();
        setupSearch();
        setupAddButton();
        setupAllExercises();
    }



    private void bindViews() {
        recyclerView = findViewById(R.id.exerciseRecyclerView);
        searchBar = findViewById(R.id.searchBar);
        addExerciseBtn = findViewById(R.id.addExerciseBtn);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvNoResults = findViewById(R.id.tvNoResults);
    }

    private void setupRecyclerView() {
        adapter = new ExerciseAdapter(new ArrayList<>());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                outRect.top = (position == 0) ? 24 : 12;
            }
        });

        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        adapter.setOnExerciseClickListener((name, met) -> {

            // HEADER CLICK
            if (met == -1) {

                if (expandedHeaders.contains(name)) {
                    expandedHeaders.remove(name);
                } else {
                    expandedHeaders.add(name);
                }

                List<ExerciseItem> filtered = new ArrayList<>();

                for (ExerciseItem e : allExercisesList) {
                    if (currentSearchText.isEmpty() ||
                            e.name.toLowerCase().contains(currentSearchText) ||
                            e.intensity.toLowerCase().contains(currentSearchText) ||
                            e.type.toLowerCase().contains(currentSearchText)) {

                        filtered.add(e);
                    }
                }

                adapter.updateList(buildSectionedList(filtered));
                return;
            }

            showSelectedExerciseDialog(name, met);
        });
        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                        int position = viewHolder.getAdapterPosition();

                        if (currentTab.equals("All")) {
                            adapter.notifyItemChanged(position);
                            return;
                        }

                        DisplayItem item = adapter.getItem(position);

                        // Prevent deleting headers or invalid items
                        if (item.isHeader || item.exerciseRef == null) {
                            adapter.notifyItemChanged(position);
                            return;
                        }

                        Exercise deletedItem = item.exerciseRef;

                        String userId = FirebaseAuth.getInstance().getUid();
                        if (userId == null) return;

                        db.collection("users")
                                .document(userId)
                                .collection("exercises")
                                .document(deletedItem.id)
                                .delete();

                        Toast.makeText(ExerciseActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onChildDraw(@NonNull Canvas c,
                                            @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder,
                                            float dX, float dY,
                                            int actionState,
                                            boolean isCurrentlyActive) {
                        if (dX == 0 && !isCurrentlyActive) {
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            return;
                        }

                        View itemView = viewHolder.itemView;

                        Paint paint = new Paint();
                        paint.setColor(Color.RED);

                        // Draw red background
                        if (dX > 0) { // Swiping right
                            c.drawRect(
                                    (float) itemView.getLeft(),
                                    (float) itemView.getTop(),
                                    dX,
                                    (float) itemView.getBottom(),
                                    paint
                            );
                        } else { // Swiping left
                            c.drawRect(
                                    (float) itemView.getRight() + dX,
                                    (float) itemView.getTop(),
                                    (float) itemView.getRight(),
                                    (float) itemView.getBottom(),
                                    paint
                            );
                        }

                        // Draw trash icon
                        Drawable icon = ContextCompat.getDrawable(
                                ExerciseActivity.this,
                                R.drawable.ic_delete
                        );

                        if (icon != null) {
                            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                            int iconTop = itemView.getTop() + iconMargin;
                            int iconBottom = iconTop + icon.getIntrinsicHeight();

                            if (dX > 0) {
                                int iconLeft = itemView.getLeft() + iconMargin;
                                int iconRight = iconLeft + icon.getIntrinsicWidth();
                                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            } else {
                                int iconRight = itemView.getRight() - iconMargin;
                                int iconLeft = iconRight - icon.getIntrinsicWidth();
                                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                            }

                            icon.draw(c);
                        }

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exercise, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.clear_history) {
            clearAllHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearAllHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Delete all exercise history? This cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {

                    String userId = FirebaseAuth.getInstance().getUid();
                    if (userId == null) return;

                    db.collection("users")
                            .document(userId)
                            .collection("exercises")
                            .get()
                            .addOnSuccessListener(query -> {

                                for (QueryDocumentSnapshot doc : query) {
                                    doc.getReference().delete();
                                }

                                todayList.clear();
                                historyList.clear();

                                applyFilters();

                                Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Today"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));
        tabLayout.addTab(tabLayout.newTab().setText("All"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                String newTab = tab.getText().toString();

                if (currentTab.equals("All") && !newTab.equals("All")) {
                    expandedHeaders.clear();
                }

                currentTab = newTab;
                searchBar.setText("");
                loadDataForTab();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .collection("exercises")
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                    todayList.clear();
                    historyList.clear();

                    int totalCalories = 0;
                    int totalTime = 0;

                    for (QueryDocumentSnapshot doc : value) {
                        Exercise e = doc.toObject(Exercise.class);
                        e.id = doc.getId();

                        if (e.date != null && e.date.equals(today)) {
                            todayList.add(e);
                            totalCalories += e.calories;
                            totalTime += e.duration;
                        } else {
                            historyList.add(e);
                        }
                    }

                    tvTotalCalories.setText(String.format("%,d", totalCalories));
                    tvTotalTime.setText(totalTime + " min");

                    loadDataForTab();
                });
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        String searchText = searchBar.getText().toString().toLowerCase();
        currentSearchText = searchText;

        adapter.setSearchQuery(searchText);

        if (currentTab.equals("All")) {

            List<ExerciseItem> filtered = new ArrayList<>();

            for (ExerciseItem e : allExercisesList) {
                if (searchText.isEmpty() ||
                        e.name.toLowerCase().contains(searchText) ||
                        e.intensity.toLowerCase().contains(searchText) ||
                        e.type.toLowerCase().contains(searchText)) {

                    filtered.add(e);
                }
            }

            expandedHeaders.clear();

            if (!searchText.isEmpty()) {
                for (ExerciseItem e : filtered) {
                    expandedHeaders.add(e.name);
                }
            }

            if (filtered.isEmpty()) {
                tvNoResults.setText("No exercises found");
                tvNoResults.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoResults.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            adapter.updateList(buildSectionedList(filtered));
            return;
        }

        List<Exercise> sourceList = currentTab.equals("Today") ? todayList : historyList;
        List<Exercise> filtered = new ArrayList<>();

        for (Exercise e : sourceList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    e.name.toLowerCase().contains(searchText);

            if (matchesSearch) {
                filtered.add(e);
            }
        }

        if (filtered.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.updateList(convertExercises(filtered));
    }

    private void loadDataForTab() {

        if (currentTab.equals("Today")) {
            adapter.setAllTab(false);

            if (todayList.isEmpty()) {
                tvNoResults.setText("No exercises yet. Add one to get started!");
                tvNoResults.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvNoResults.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateList(convertExercises(todayList));
            }

        } else if (currentTab.equals("History")) {
                adapter.setAllTab(false);

                if (historyList.isEmpty()) {
                    tvNoResults.setText("No history yet.");
                    tvNoResults.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvNoResults.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.updateList(convertExercises(historyList));
                }
        } else {
            adapter.setAllTab(true);
            adapter.updateList(buildSectionedList(allExercisesList));
        }
    }

    private List<DisplayItem> convertExercises(List<Exercise> list) {
        List<DisplayItem> result = new ArrayList<>();

        for (Exercise e : list) {
            ExerciseItem item = new ExerciseItem(e.name, "", e.type, 0);

            item.intensity = e.duration + " min • " + e.calories + " cal";
            DisplayItem displayItem = new DisplayItem(item);
            displayItem.exerciseRef = e;
            result.add(displayItem);

        }

        return result;
    }

    private List<DisplayItem> buildSectionedList(List<ExerciseItem> list) {

        Map<String, List<ExerciseItem>> grouped = new HashMap<>();

        for (ExerciseItem e : list) {
            grouped.computeIfAbsent(e.name, k -> new ArrayList<>()).add(e);
        }

        List<DisplayItem> result = new ArrayList<>();
        List<String> names = new ArrayList<>(grouped.keySet());
        Collections.sort(names);

        for (String name : names) {

            DisplayItem header = new DisplayItem(name);
            header.expanded = expandedHeaders.contains(name);
            result.add(header);

            if (header.expanded) {
                List<ExerciseItem> exercises = grouped.get(name);

                exercises.sort((a, b) ->
                        getOrderValue(a.intensity) - getOrderValue(b.intensity)
                );

                for (ExerciseItem e : exercises) {
                    result.add(new DisplayItem(e));
                }
            }
        }

        return result;
    }

    private void showCustomExerciseDialog() {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_custom_exercise, null);

        EditText etName = view.findViewById(R.id.etName);
        EditText etDuration = view.findViewById(R.id.etDuration);
        EditText etCalories = view.findViewById(R.id.etCalories);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Custom Exercise")
                .setView(view)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            addBtn.setOnClickListener(v -> {

                String name = etName.getText().toString().trim();
                String durationStr = etDuration.getText().toString().trim();
                String caloriesStr = etCalories.getText().toString().trim();

                if (name.isEmpty()) {
                    etName.setError("Required");
                    return;
                }

                if (durationStr.isEmpty()) {
                    etDuration.setError("Required");
                    return;
                }

                if (caloriesStr.isEmpty()) {
                    etCalories.setError("Required");
                    return;
                }

                int duration, calories;

                try {
                    duration = Integer.parseInt(durationStr);
                } catch (Exception e) {
                    etDuration.setError("Invalid number");
                    return;
                }

                try {
                    calories = Integer.parseInt(caloriesStr);
                } catch (Exception e) {
                    etCalories.setError("Invalid number");
                    return;
                }

                if (duration <= 0) {
                    etDuration.setError("Must be > 0");
                    return;
                }

                if (calories <= 0) {
                    etCalories.setError("Must be > 0");
                    return;
                }

                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                String userId = FirebaseAuth.getInstance().getUid();
                if (userId == null) return;

                String id = db.collection("users")
                        .document(userId)
                        .collection("exercises")
                        .document()
                        .getId();

                Exercise exercise = new Exercise(name, calories, duration, "Custom", date);

                db.collection("users")
                        .document(userId)
                        .collection("exercises")
                        .document(id)
                        .set(exercise);

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private int getOrderValue(String intensity) {
        intensity = intensity.toLowerCase();

        if (intensity.contains("leisure") || intensity.contains("light")) return 1;
        if (intensity.contains("slow")) return 2;
        if (intensity.contains("moderate")) return 3;
        if (intensity.contains("fast")) return 4;
        if (intensity.contains("very fast") || intensity.contains("vigorous")) return 5;

        return 100;
    }

    private void setupAllExercises() {
        try {
            InputStream is = getAssets().open("exercises.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder json = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            JSONArray array = new JSONArray(json.toString());

            allExercisesList.clear();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                String name = obj.getString("name");
                String intensity = obj.getString("intensity");
                String type = obj.getString("type");
                double met = obj.getDouble("met");

                allExercisesList.add(new ExerciseItem(name, intensity, type, met));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading exercises", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAddButton() {
        addExerciseBtn.setOnClickListener(v -> showCustomExerciseDialog());
    }



    private void loadUserWeight() {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) return;

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("weightInPounds")) {
                        double pounds = document.getDouble("weightInPounds");
                        userWeight = pounds * 0.453592;
                    }
                });
    }

    private void showSelectedExerciseDialog(String exerciseName, double met) {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_exercises, null);

        EditText durInput = view.findViewById(R.id.etDuration);
        TextView exerciseText = view.findViewById(R.id.tvSelectedExercise);
        TextView caloriesText = view.findViewById(R.id.tvEstimatedCalories);

        exerciseText.setText(exerciseName);

        durInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().isEmpty()) {
                    caloriesText.setText("Estimated Calories: 0");
                    return;
                }

                int dur = Integer.parseInt(s.toString());
                int cal = (int) (met * 3.5 * userWeight / 200 * dur);

                caloriesText.setText("Estimated Calories: " + cal);
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Add Exercise")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {

                    String durStr = durInput.getText().toString().trim();
                    if (durStr.isEmpty()) return;

                    int dur = Integer.parseInt(durStr);
                    int cal = (int) (met * 3.5 * userWeight / 200 * dur);

                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                    String userId = FirebaseAuth.getInstance().getUid();
                    if (userId == null) return;

                    String id = db.collection("users")
                            .document(userId)
                            .collection("exercises")
                            .document()
                            .getId();

                    Exercise exercise = new Exercise(exerciseName, cal, dur, "Custom", date);

                    db.collection("users")
                            .document(userId)
                            .collection("exercises")
                            .document(id)
                            .set(exercise);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}