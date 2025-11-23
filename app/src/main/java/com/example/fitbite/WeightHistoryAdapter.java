package com.example.fitbite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.Holder> {

    private ArrayList<WeightEntry> list;

    public WeightHistoryAdapter(ArrayList<WeightEntry> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_history, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        WeightEntry e = list.get(position);
        holder.weight.setText(e.weight + " lb");
        holder.dateTime.setText(e.date + " • " + e.time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView weight, dateTime;

        public Holder(@NonNull View itemView) {
            super(itemView);

            weight = itemView.findViewById(R.id.itemWeight);
            dateTime = itemView.findViewById(R.id.itemDateTime);
        }
    }
}
