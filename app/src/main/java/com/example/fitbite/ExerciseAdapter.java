package com.example.fitbite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DisplayItem> fullList;
    private List<DisplayItem> filteredList;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EXERCISE = 1;

    private boolean isAllTab = false;

    private String searchQuery = "";

    public interface OnExerciseClickListener {
        void onExerciseClick(String name, double met);
    }

    private OnExerciseClickListener listener;

    public void setOnExerciseClickListener(OnExerciseClickListener listener) {
        this.listener = listener;
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query.toLowerCase();
    }
    public ExerciseAdapter(List<DisplayItem> list) {
        this.fullList = new ArrayList<>(list);
        this.filteredList = new ArrayList<>(list);
    }

    public void setAllTab(boolean isAllTab) {
        this.isAllTab = isAllTab;
    }


    @Override
    public int getItemViewType(int position) {
        return filteredList.get(position).isHeader ? TYPE_HEADER : TYPE_EXERCISE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DisplayItem item = filteredList.get(position);

        if (item.isHeader) {
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.title.setText(item.headerTitle);
            h.arrow.setRotation(item.expanded ? 180f : 0f);

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(item.headerTitle, -1);
                }
            });

        } else {
            ExerciseViewHolder eHolder = (ExerciseViewHolder) holder;
            ExerciseItem e = item.exercise;

            String displayName = e.name;

            if (e.intensity != null && !e.intensity.isEmpty()) {

                if (e.intensity.contains("min")) {
                    // History / Today
                    eHolder.tvDetails.setText(e.intensity);
                    eHolder.tvDetails.setVisibility(View.VISIBLE);

                } else {
                    // All tab
                    displayName += " (" + e.intensity + ")";
                    eHolder.tvDetails.setVisibility(View.GONE);
                }

            } else {
                eHolder.tvDetails.setVisibility(View.GONE);
            }

            if (!searchQuery.isEmpty() && displayName.toLowerCase().contains(searchQuery)) {

                int start = displayName.toLowerCase().indexOf(searchQuery);
                int end = start + searchQuery.length();

                SpannableString spannable = new SpannableString(displayName);
                spannable.setSpan(
                        new ForegroundColorSpan(Color.BLUE),
                        start,
                        end,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                eHolder.tvName.setText(spannable);

            } else {
                eHolder.tvName.setText(displayName);
            }

            if (isAllTab) {
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onExerciseClick(e.name, e.met);
                    }
                });
            } else {
                holder.itemView.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView arrow;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvHeader);
            arrow = itemView.findViewById(R.id.ivArrow);
        }
    }

    public void updateList(List<DisplayItem> newList) {
        this.fullList = new ArrayList<>(newList);
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDetails;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvDetails = itemView.findViewById(R.id.tvExerciseDetails);
        }
    }
    public DisplayItem getItem(int position) {

        return filteredList.get(position);
    }
}