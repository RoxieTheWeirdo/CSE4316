package com.example.fitbite;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private Context context;
    private List<FoodItem> foodList;

    public FoodAdapter(Context context, List<FoodItem> foodList) {
        this.context = context;
        this.foodList = foodList;
    }
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodTextView;
        Button editButton;
        Button plusButton;
        public FoodViewHolder(View itemView) {
            super(itemView);
            foodTextView = itemView.findViewById(R.id.foodTextView);
            editButton = itemView.findViewById(R.id.editButton);
            plusButton = itemView.findViewById(R.id.plusButton);
        }
    }
    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.foodlist, parent, false);
        return new FoodViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);
        holder.foodTextView.setText(item.name + " " + item.calories);

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditFoodActivity.class);
            intent.putExtra("foodName", item.name);
            intent.putExtra("calories", item.calories);
            context.startActivity(intent);
        });

        // + button has no functionality for now
        holder.plusButton.setOnClickListener(v -> {
            //This is the plus button, does nothing for now
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }
}
