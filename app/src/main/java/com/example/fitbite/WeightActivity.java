package com.example.fitbite;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class WeightActivity extends AppCompatActivity {

    //UI Elements
    private TextView currentWeightText, percentText, goalProgressText, lastUpdatedText;
    private CircularProgressIndicator progressRing;
    private LineChart weekChart, monthChart, yearChart;

    //Sample data for now
    private float currentWeight = 164.2f;
    private float goalWeight = 150f;

    //User entered weight history
    private ArrayList<WeightEntry> historyList = new ArrayList<>();
    private WeightHistoryAdapter historyAdapter;

    // TEMPORARY SAMPLE DATA FOR TESTING CHARTS
    private ArrayList<Float> sampleWeights = new ArrayList<>(
            Arrays.asList(
                    165f, 164.4f, 164.1f, 163.9f, 163.4f, 162.8f, 162.3f,   // WEEK
                    162.2f, 162.0f, 161.8f, 161.6f, 161.4f, 161.2f, 161.0f, 160.8f, // MONTH
                    160.5f, 160.3f, 159.9f, 159.6f, 159.4f, 159.2f, 158.9f, 158.7f,
                    158.5f, 158.3f, 158.1f, 157.9f, 157.8f, 157.6f, 157.4f          // YEAR filler
            )
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        //Find Views
        currentWeightText = findViewById(R.id.currentWeightInsideValue);
        percentText = findViewById(R.id.currentWeightPercent);
        goalProgressText = findViewById(R.id.goalProgressInfo);
        lastUpdatedText = findViewById(R.id.lastUpdatedText);
        progressRing = findViewById(R.id.currentWeightProgress);
        weekChart = findViewById(R.id.weekChart);
        monthChart = findViewById(R.id.monthChart);
        yearChart = findViewById(R.id.yearChart);

        Button logWeightButton = findViewById(R.id.logWeightButton);

        //Setup Tabs
        TabLayout weightTabs = findViewById(R.id.weightTabs);
        weightTabs.addTab(weightTabs.newTab().setText("WEEK"));
        weightTabs.addTab(weightTabs.newTab().setText("MONTH"));
        weightTabs.addTab(weightTabs.newTab().setText("YEAR"));

        //Chart marker dismissal
        enableMarkerDismiss(weekChart);
        enableMarkerDismiss(monthChart);
        enableMarkerDismiss(yearChart);
        enableOutsideTapToDismiss(weekChart);
        enableOutsideTapToDismiss(monthChart);
        enableOutsideTapToDismiss(yearChart);

        historyAdapter = new WeightHistoryAdapter(historyList);

        weightTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Reset marker popup on tab switch
                hideAllMarkers();

                switch (tab.getPosition()) {
                    case 0:
                        showChart(weekChart, monthChart, yearChart);
                        loadWeeklyData();
                        break;
                    case 1:
                        showChart(monthChart, weekChart, yearChart);
                        loadMonthlyData();
                        break;
                    case 2:
                        showChart(yearChart, weekChart, monthChart);
                        loadYearlyData();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        //Ensure fireworks appear above everything
        KonfettiView konfettiView = findViewById(R.id.confettiView);
        konfettiView.bringToFront();

        //History popup button
        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> showHistoryPopup());

        //Load UI and charts
        loadWeeklyData();
        updateUI();

        //Log weight button
        logWeightButton.setOnClickListener(v -> showLogWeightDialog());

        //Set new goal weight button
        Button setGoalButton = findViewById(R.id.setGoalButton);
        setGoalButton.setOnClickListener(v -> showSetGoalDialog());
    }

    //UI UPDATE
    private void updateUI() {
        currentWeightText.setText(formatWeight(currentWeight));
        updateProgressRing();
        updateGoalLine();
    }

    private String formatWeight(float w) {
        if (w == (int) w) return (int) w + "";
        return String.format("%.1f", w);
    }

    private void updateProgressRing() {
        //Calculate progress toward goal
        float remaining = Math.abs(currentWeight - goalWeight);
        float percent = 100f - ((remaining / currentWeight) * 100f);
        percent = Math.max(0, Math.min(100, percent));

        progressRing.setIndeterminate(false);
        progressRing.setProgressCompat((int) percent, true);
        percentText.setText((int) percent + "% to goal");

        if (remaining < 0.1f)
            showFireworks();
    }

    private void updateGoalLine() {
        float remaining = Math.abs(currentWeight - goalWeight);

        if (remaining < 0.1f) {
            goalProgressText.setText("Goal Reached!");
        } else {
            goalProgressText.setText(
                    "Goal: " + formatWeight(goalWeight)
                            + " lb • " + formatWeight(remaining)
                            + " lb remaining"
            );
        }
    }

    //Swap visible chart based on selected tab
    private void showChart(LineChart show, LineChart hide1, LineChart hide2) {
        show.setVisibility(View.VISIBLE);
        hide1.setVisibility(View.GONE);
        hide2.setVisibility(View.GONE);
    }

    //FIREWORKS
    private void showFireworks() {
        KonfettiView konfettiView = findViewById(R.id.confettiView);

        List<Integer> colors = Arrays.asList(
                0xFFE91E63, 0xFFFFC107, 0xFF3F51B5, 0xFF4CAF50, 0xFFFF5722
        );

        EmitterConfig emitterConfig =
                new Emitter(300L, TimeUnit.MILLISECONDS).perSecond(250);

        PartyFactory base = new PartyFactory(emitterConfig)
                .angle(0)
                .spread(360)
                .sizes(Arrays.asList(
                        new Size(6, 8f, 1),
                        new Size(10, 12f, 1),
                        new Size(14, 16f, 1)
                ))
                .shapes(Arrays.asList(
                        Shape.Circle.INSTANCE,
                        Shape.Square.INSTANCE
                ))
                .colors(colors)
                .timeToLive(1800L);

        konfettiView.start(
                base.position(new Position.Relative(0.5, 0.25)).build()
        );
    }

    //DIALOG: LOG WEIGHT
    private void showLogWeightDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.log_weight, null);

        EditText etWeight = dialogView.findViewById(R.id.etWeight);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etTime = dialogView.findViewById(R.id.etTime);

        Calendar c = Calendar.getInstance();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat =
                new SimpleDateFormat("hh:mm a", Locale.getDefault());

        etDate.setText(dateFormat.format(c.getTime()));
        etTime.setText(timeFormat.format(c.getTime()));

        //Date picker popup
        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(
                    WeightActivity.this,
                    (view, year, month, day) -> {
                        Calendar c2 = Calendar.getInstance();
                        c2.set(year, month, day);
                        etDate.setText(dateFormat.format(c2.getTime()));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        //Time picker popup
        etTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(
                    WeightActivity.this,
                    (view, hour, minute) -> {
                        Calendar c3 = Calendar.getInstance();
                        c3.set(Calendar.HOUR_OF_DAY, hour);
                        c3.set(Calendar.MINUTE, minute);
                        etTime.setText(timeFormat.format(c3.getTime()));
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false
            ).show();
        });

        //Save weight dialog
        new AlertDialog.Builder(this)
                .setTitle("Log Your Weight")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String w = etWeight.getText().toString().trim();

                    if (w.isEmpty()) {
                        Toast.makeText(this, "Enter weight first",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Update current weight
                    currentWeight = Float.parseFloat(w);
                    updateUI();

                    //Add entry to history
                    historyList.add(0, new WeightEntry(
                            currentWeight,
                            etDate.getText().toString(),
                            etTime.getText().toString()
                    ));

                    historyAdapter.notifyDataSetChanged();

                    lastUpdatedText.setText(
                            "Last updated: " +
                                    etDate.getText() + " • " +
                                    etTime.getText()
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // WEEK / MONTH / YEAR CHARTS
    private void loadWeeklyData() {
        int days = 7;
        float[] values = new float[days];
        String[] labels = new String[days];

        Calendar today = Calendar.getInstance();
        Calendar day = (Calendar) today.clone();
        day.add(Calendar.DAY_OF_YEAR, -(days - 1));

        SimpleDateFormat entryFormat =
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat labelFormat =
                new SimpleDateFormat("EEE", Locale.getDefault());

        Float lastKnown = null;

        for (int i = 0; i < days; i++) {

            Float weightForDay = findWeightForDay(day, entryFormat);

            if (weightForDay != null) lastKnown = weightForDay;

            //DO NOT DELETE --- commented off for sample data
            //values[i] = (lastKnown != null ? lastKnown : currentWeight);
            values[i] = sampleWeights.get(i); //TEMPORARY SAMPLE DATA
            labels[i] = labelFormat.format(day.getTime());

            day.add(Calendar.DAY_OF_YEAR, 1);
        }

        configureWeekChart();
        loadChart(weekChart, values, labels);
    }

    private void loadMonthlyData() {
        int days = 30;
        float[] values = new float[days];
        String[] labels = new String[days];

        Calendar today = Calendar.getInstance();
        Calendar day = (Calendar) today.clone();
        day.add(Calendar.DAY_OF_YEAR, -(days - 1));

        SimpleDateFormat entryFormat =
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat labelFormat =
                new SimpleDateFormat("MMM d", Locale.getDefault());

        Float lastKnown = null;

        for (int i = 0; i < days; i++) {

            Float weightForDay = findWeightForDay(day, entryFormat);

            if (weightForDay != null) lastKnown = weightForDay;

            //DO NOT DELETE --- commented off for sample data
            //values[i] = (lastKnown != null ? lastKnown : currentWeight);
            values[i] = sampleWeights.get(i); //TEMPORARY SAMPLE DATA
            labels[i] = labelFormat.format(day.getTime());

            day.add(Calendar.DAY_OF_YEAR, 1);
        }

        configureScrollChart(monthChart);
        loadChart(monthChart, values, labels);

        //Scroll to end on load
        monthChart.post(() -> {
            monthChart.moveViewToX(values.length - 1);
        });
    }

    private void loadYearlyData() {
        int months = 12;
        float[] values = new float[months];
        String[] labels = new String[months];

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, -(months - 1));

        SimpleDateFormat entryFormat =
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat labelFormat =
                new SimpleDateFormat("MMM", Locale.getDefault());

        Float lastKnown = null;

        for (int i = 0; i < months; i++) {

            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);

            labels[i] = labelFormat.format(cal.getTime());

            Float monthWeight =
                    getLastWeightForMonth(year, month, entryFormat);

            if (monthWeight != null) lastKnown = monthWeight;

            //DO NOT DELETE --- commented off for sample data
            //values[i] = (lastKnown != null ? lastKnown : currentWeight);
            values[i] = sampleWeights.get(i); //TEMPORARY SAMPLE DATA

            cal.add(Calendar.MONTH, 1);
        }

        configureScrollChart(yearChart);
        configureYearChart();
        loadChart(yearChart, values, labels);

        //Scroll to end on load
        yearChart.post(() -> {
            yearChart.moveViewToX(values.length - 1);
        });
    }

    //Chart styling
    private void configureYearChart() {
        yearChart.setDragEnabled(true);
        yearChart.setScaleXEnabled(true);
        yearChart.setScaleYEnabled(false);
        yearChart.setPinchZoom(true);

        yearChart.getDescription().setEnabled(false);

        XAxis x = yearChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setGranularityEnabled(true);

        // Show 6 months at a time
        yearChart.setVisibleXRangeMinimum(6f);
        yearChart.setVisibleXRangeMaximum(6f);

        yearChart.moveViewToX(0f);
    }

    private void configureWeekChart() {
        weekChart.setDragEnabled(false);
        weekChart.setScaleXEnabled(false);
        weekChart.setScaleYEnabled(false);
        weekChart.setPinchZoom(false);
        weekChart.setDoubleTapToZoomEnabled(false);

        XAxis x = weekChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelCount(7, true);
        x.setGranularity(1f);
        x.setGranularityEnabled(true);
    }

    private void configureScrollChart(LineChart chart) {
        chart.setDragEnabled(true);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(true);
    }

    //HISTORY LOOKUP HELPERS
    private Float findWeightForDay(Calendar targetDay,
                                   SimpleDateFormat entryFormat) {

        for (WeightEntry entry : historyList) {
            try {
                java.util.Date d = entryFormat.parse(entry.getDate());
                Calendar c = Calendar.getInstance();
                c.setTime(d);

                boolean sameDay =
                        c.get(Calendar.YEAR) ==
                                targetDay.get(Calendar.YEAR)
                                &&
                                c.get(Calendar.DAY_OF_YEAR) ==
                                        targetDay.get(Calendar.DAY_OF_YEAR);

                if (sameDay) return entry.getWeight();

            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Float getLastWeightForMonth(int targetYear,
                                        int targetMonth,
                                        SimpleDateFormat entryFormat) {

        java.util.Date latestDate = null;
        Float lastWeight = null;

        for (WeightEntry entry : historyList) {
            try {
                java.util.Date d = entryFormat.parse(entry.getDate());
                Calendar c = Calendar.getInstance();
                c.setTime(d);

                if (c.get(Calendar.YEAR) == targetYear &&
                        c.get(Calendar.MONTH) == targetMonth) {

                    if (latestDate == null || d.after(latestDate)) {
                        latestDate = d;
                        lastWeight = entry.getWeight();
                    }
                }

            } catch (Exception ignored) {
            }
        }
        return lastWeight;
    }

    //Load data into chart
    private void loadChart(LineChart chart, float[] values, String[] labels) {

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            entries.add(new Entry(i, values[i]));
        }

        //Styled line dataset
        LineDataSet ds = new LineDataSet(entries, "");
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setLineWidth(3f);
        ds.setColor(Color.parseColor("#FF4081"));
        ds.setDrawCircles(false);
        ds.setDrawValues(false);
        ds.setDrawFilled(true);

        Drawable gradient =
                ContextCompat.getDrawable(this, R.drawable.weight_gradient);
        ds.setFillDrawable(gradient);

        chart.setData(new LineData(ds));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        //X-axis label settings
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < labels.length) ? labels[i] : "";
            }
        });

        //Attach marker popup view
        WeightMarkerView marker = new WeightMarkerView(
                this,
                R.layout.marker_weight,
                labels
        );
        marker.setChartView(chart);
        chart.setMarker(marker);
        chart.invalidate();

        //Reset chart on switch
        if (chart == yearChart) {
            chart.post(() -> {
                chart.fitScreen();
                chart.setVisibleXRangeMaximum(6f);
                chart.setVisibleXRangeMinimum(6f);
                chart.moveViewToX(0f);
            });
        }

        if (chart == monthChart) {
            chart.post(() -> {
                chart.fitScreen();
                chart.setVisibleXRangeMinimum(10f);
                chart.setVisibleXRangeMaximum(10f);
                chart.moveViewToX(0f);
            });
        }
    }

    //Marker behavior
    private void enableMarkerDismiss(LineChart chart) {
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                // marker appears automatically
            }

            @Override
            public void onNothingSelected() {
                chart.highlightValues(null); // hides marker
            }
        });
    }

    private void hideAllMarkers() {
        weekChart.highlightValues(null);
        monthChart.highlightValues(null);
        yearChart.highlightValues(null);
    }

    private void enableOutsideTapToDismiss(LineChart chart) {
        chart.setOnTouchListener((v, event) -> {
            if (chart.getHighlighted() != null && chart.getHighlighted().length > 0) {
                chart.highlightValues(null);
            }
            return false;
        });
    }

    //History popup
    private void showHistoryPopup() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View view = getLayoutInflater()
                .inflate(R.layout.history_bottom_sheet, null);

        RecyclerView popupRecycler = view.findViewById(R.id.historyRecyclerPopup);
        popupRecycler.setLayoutManager(new LinearLayoutManager(this));
        popupRecycler.setAdapter(historyAdapter);
        popupRecycler.setNestedScrollingEnabled(true);

        TextView clearBtn = view.findViewById(R.id.clearHistoryButton);
        clearBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to delete all logged weights?")
                    .setPositiveButton("Clear", (d, i) -> {
                        historyList.clear();
                        historyAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "History cleared",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        dialog.setContentView(view);
        dialog.getBehavior().setFitToContents(true);
        dialog.show();
    }

    //Set new goal dialog
    private void showSetGoalDialog() {
        View view = getLayoutInflater()
                .inflate(R.layout.dialog_set_goal, null);

        EditText etGoal = view.findViewById(R.id.etGoal);

        new AlertDialog.Builder(this)
                .setTitle("Set New Goal Weight")
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    String goalStr = etGoal.getText().toString().trim();
                    if (goalStr.isEmpty()) {
                        Toast.makeText(this, "Enter a valid goal weight",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    goalWeight = Float.parseFloat(goalStr);
                    updateUI();
                    Toast.makeText(this, "Goal Updated!",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}