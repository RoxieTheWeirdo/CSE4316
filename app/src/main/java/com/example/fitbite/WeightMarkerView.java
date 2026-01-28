package com.example.fitbite;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class WeightMarkerView extends MarkerView {

    private final TextView tvWeight, tvDate;
    private final String[] labels;

    public WeightMarkerView(Context context, int layoutResource, String[] labels) {
        super(context, layoutResource);
        this.labels = labels;

        tvWeight = findViewById(R.id.tvMarkerWeight);
        tvDate   = findViewById(R.id.tvMarkerDate);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvWeight.setText(String.format("%.1f lb", e.getY()));
        tvDate.setText(labels[(int) e.getX()]);

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight() - 20);
    }
}