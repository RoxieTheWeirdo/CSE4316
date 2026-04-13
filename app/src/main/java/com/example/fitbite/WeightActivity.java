package com.example.fitbite;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private LineChart weightChart;
    private float currentWeight = 0f;
    private float goalWeight = 150f;
    private ArrayList<WeightEntry> historyList = new ArrayList<>();
    private WeightHistoryAdapter historyAdapter;
    private boolean hasGoal = false;

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
        weightChart = findViewById(R.id.weightChart);

        Button logWeightButton = findViewById(R.id.logWeightButton);
        Button setGoalButton = findViewById(R.id.setGoalButton);
        Button historyButton = findViewById(R.id.historyButton);

        loadSavedWeightAndDate();

        //Ensure fireworks appear above everything
        KonfettiView konfettiView = findViewById(R.id.confettiView);
        konfettiView.bringToFront();

        //History popup button
        historyAdapter = new WeightHistoryAdapter(historyList);
        historyButton.setOnClickListener(v -> showHistoryPopup());

        loadHistoryFromFirebase(null);

        //Load UI and charts
        updateUI();

        //Log weight button
        logWeightButton.setOnClickListener(v -> showLogWeightDialog());
        //Set new goal weight button
        setGoalButton.setOnClickListener(v -> showSetGoalDialog());
    }

    private void loadSavedWeightAndDate() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) return;

                        if (doc.exists()) {

                            if (doc.contains("weightInPounds")) {
                                currentWeight = doc.getDouble("weightInPounds").floatValue();
                            }

                            if (doc.contains("goalWeight")) {
                                goalWeight = doc.getDouble("goalWeight").floatValue();
                                hasGoal = true;
                            } else {
                                hasGoal = false;
                            }

                            if (doc.contains("lastUpdatedDate") && doc.contains("lastUpdatedTime")) {
                                String d = doc.getString("lastUpdatedDate");
                                String t = doc.getString("lastUpdatedTime");
                                lastUpdatedText.setText("Last updated: " + d + " • " + t);
                            }

                            updateUI();
                            if (!hasGoal) {
                                showSetGoalDialogForced();
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load weight", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void showSetGoalDialogForced() {
        View view = getLayoutInflater()
                .inflate(R.layout.dialog_set_goal, null);

        EditText etGoal = view.findViewById(R.id.etGoal);

        new AlertDialog.Builder(this)
                .setTitle("Set Your Goal Weight")
                .setMessage("You need to set a goal before using this page.")
                .setView(view)
                .setCancelable(false) // cannot dismiss
                .setPositiveButton("Save", (dialog, which) -> {
                    String goalStr = etGoal.getText().toString().trim();

                    if (goalStr.isEmpty()) {
                        Toast.makeText(this, "Please enter a goal weight", Toast.LENGTH_SHORT).show();
                        showSetGoalDialogForced(); // reopen
                        return;
                    }

                    goalWeight = Float.parseFloat(goalStr);
                    hasGoal = true;
                    showUI();
                    updateUI();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> data = new HashMap<>();
                        data.put("goalWeight", goalWeight);

                        db.collection("users")
                                .document(user.getUid())
                                .set(data, SetOptions.merge());
                    }

                    Toast.makeText(this, "Goal saved!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }


    //UI UPDATE
    private void updateUI() {
        currentWeightText.setText(formatWeight(currentWeight));

        if (!hasGoal) {
            hideUI();
            return;
        }
        showUI();
        updateProgressRing();
        updateGoalText();
        updateGoalLine(goalWeight);
    }

    private void hideUI() {
        progressRing.setVisibility(View.INVISIBLE);
        percentText.setVisibility(View.INVISIBLE);
        goalProgressText.setVisibility(View.INVISIBLE);
    }

    private void showUI() {
        progressRing.setVisibility(View.VISIBLE);
        percentText.setVisibility(View.VISIBLE);
        goalProgressText.setVisibility(View.VISIBLE);
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

    private void updateGoalText() {
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

    private void saveToFirebase(float weight, String date, String time) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("weightInPounds", weight);
        data.put("lastUpdatedDate", date);
        data.put("lastUpdatedTime", time);

        db.collection("users").document(user.getUid())
                .set(data, SetOptions.merge());
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
                    // Save to Firebase
                    saveToFirebase(
                            currentWeight,
                            etDate.getText().toString(),
                            etTime.getText().toString()
                    );


                    //SAVE HISTORY ENTRY TO FIREBASE SUBCOLLECTION
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        Map<String, Object> historyEntry = new HashMap<>();
                        historyEntry.put("weight", currentWeight);
                        historyEntry.put("date", etDate.getText().toString());
                        historyEntry.put("time", etTime.getText().toString());
                        historyEntry.put("timestamp", System.currentTimeMillis());


                        db.collection("users")
                                .document(user.getUid())
                                .collection("weightHistory")
                                .add(historyEntry)
                                .addOnSuccessListener(ref -> {
                                    Toast.makeText(this, "History saved!", Toast.LENGTH_SHORT).show();
                                    loadHistoryFromFirebase(null);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "FAILED: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "FAILED: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }

                    lastUpdatedText.setText(
                            "Last updated: " +
                                    etDate.getText() + " • " +
                                    etTime.getText()
                    );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSetGoalDialog() {
        View view = getLayoutInflater()
                .inflate(R.layout.dialog_set_goal, null);

        EditText etGoal = view.findViewById(R.id.etGoal);

        etGoal.setText(formatWeight(goalWeight));
        etGoal.setSelection(etGoal.getText().length());

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
                    updateGoalText();
                    updateGoalLine(goalWeight);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> data = new HashMap<>();
                        data.put("goalWeight", goalWeight);
                        db.collection("users")
                                .document(user.getUid())
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Goal Updated!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Goal Updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void loadHistoryFromFirebase(Runnable callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("weightHistory")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(query -> {

                    ArrayList<WeightEntry> fullHistory = new ArrayList<>();

                    for (var doc : query.getDocuments()) {
                        Double w = doc.getDouble("weight");
                        String d = doc.getString("date");
                        String t = doc.getString("time");
                        Long ts = doc.getLong("timestamp");

                        if (w != null && d != null) {
                            fullHistory.add(new WeightEntry(
                                    w.floatValue(),
                                    d,
                                    (t != null ? t : ""),
                                    (ts != null ? ts : 0L)
                            ));
                        }
                    }



                    // ---- HISTORY LIST (newest first) ----
                    historyList.clear();
                    historyList.addAll(fullHistory);
                    historyList.sort((a, b) -> {
                        try {
                            return new SimpleDateFormat(
                                    "MMM dd, yyyy hh:mm a", Locale.getDefault()
                            ).parse(b.getDate() + " " + b.getTime())
                                    .compareTo(
                                            new SimpleDateFormat(
                                                    "MMM dd, yyyy hh:mm a", Locale.getDefault()
                                            ).parse(a.getDate() + " " + a.getTime())
                                    );
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    historyAdapter.notifyDataSetChanged();

                    // ---- CHART DATA (one point per day) ----
                    ArrayList<WeightEntry> chartData =
                            getLatestWeightPerDay(fullHistory);

                    loadWeightChart(chartData);

                    if (callback != null) callback.run();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load history",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

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
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(user.getUid())
                                    .collection("weightHistory")
                                    .get()
                                    .addOnSuccessListener(query -> {
                                        for (var doc : query.getDocuments()) {
                                            doc.getReference().delete();
                                        }
                                    });
                        }
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

    private void loadWeightChart(ArrayList<WeightEntry> chartData) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Sort oldest → newest for chart timeline
        chartData.sort((a, b) -> {
            try {
                return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .parse(a.getDate())
                        .compareTo(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .parse(b.getDate()));
            } catch (Exception e) {
                return 0;
            }
        });

        SimpleDateFormat inputFormat =
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat shortDate =
                new SimpleDateFormat("MMM d", Locale.getDefault());
        // If no history yet, seed with current weight
        if (chartData.isEmpty()) {
            chartData.add(new WeightEntry(
                    currentWeight,
                    new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date()),
                    new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()),
                    System.currentTimeMillis()
            ));
        }

        for (int i = 0; i < chartData.size(); i++) {
            WeightEntry entry = chartData.get(i);
            entries.add(new Entry(i, entry.getWeight()));

            try {
                labels.add(shortDate.format(inputFormat.parse(entry.getDate())));
            } catch (Exception e) {
                labels.add(entry.getDate()); // fallback
            }
        }
        boolean isSinglePoint = entries.size() == 1;

        LineDataSet ds = new LineDataSet(entries, "");
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setCubicIntensity(0.2f);
        ds.setLineWidth(3f);
        ds.setColor(Color.parseColor("#6FBF73"));
        ds.setCircleColor(Color.parseColor("#6FBF73"));
        ds.setDrawValues(false);
        ds.setDrawFilled(true);
        ds.setFillColor(Color.parseColor("#DFF5E1"));
        ds.setFillAlpha(120);
        ds.setDrawCircles(isSinglePoint);
        ds.setCircleRadius(6f);
        ds.setDrawFilled(!isSinglePoint);

        LineData data = new LineData(ds);
        weightChart.setData(data);
        int count = entries.size();

        if (count <= 1) {
            weightChart.setVisibleXRangeMaximum(1);
            weightChart.moveViewToX(0);
            weightChart.setDragEnabled(false);
        } else {
            // Always show up to 7 points
            weightChart.setVisibleXRangeMaximum(Math.min(7, count));

            // Snap to latest entry
            weightChart.moveViewToX(Math.max(0, count - 7));

            weightChart.setDragEnabled(count > 7);
        }

        weightChart.setDragDecelerationEnabled(false);
        weightChart.setDragEnabled(true);
        weightChart.setScaleXEnabled(false);
        weightChart.setScaleYEnabled(false);
        weightChart.setDragDecelerationEnabled(true);
        weightChart.setDragDecelerationFrictionCoef(0.9f);
        weightChart.setHighlightPerTapEnabled(false);

        XAxis xAxis = weightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        if (isSinglePoint) {
            // lock X axis to one value
            xAxis.setAxisMinimum(0f);
            xAxis.setAxisMaximum(0f);
            xAxis.setLabelCount(1, false);
        } else {
            xAxis.resetAxisMinimum();
            xAxis.resetAxisMaximum();
            xAxis.setLabelCount(Math.min(7, labels.size()), false);
        }


        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                return (i >= 0 && i < labels.size()) ? labels.get(i) : "";
            }
        });
        xAxis.setTextColor(Color.parseColor("#6B6B6B"));
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(false);

        YAxis leftAxis = weightChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#EEEEEE"));
        leftAxis.setTextColor(Color.parseColor("#6B6B6B"));
        if (isSinglePoint) {
            float y = entries.get(0).getY();
            leftAxis.setAxisMinimum(y - 5f);
            leftAxis.setAxisMaximum(y + 5f);
        } else {
            leftAxis.resetAxisMinimum();
            leftAxis.resetAxisMaximum();
        }

        weightChart.getAxisLeft().removeAllLimitLines();
        LimitLine goalLine = new LimitLine(goalWeight, "Goal");
        goalLine.setLineColor(Color.parseColor("#9CCC9C"));
        goalLine.setLineWidth(1.5f);
        goalLine.setTextColor(Color.parseColor("#6FBF73"));
        goalLine.enableDashedLine(10f, 10f, 0f);
        goalLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        weightChart.getAxisLeft().addLimitLine(goalLine);
        weightChart.getAxisRight().setEnabled(false);
        weightChart.getDescription().setEnabled(false);
        weightChart.animateX(600);
        weightChart.setExtraBottomOffset(24f);
        weightChart.setClipToPadding(false);
        weightChart.invalidate();
        weightChart.setScaleEnabled(false);
        weightChart.setPinchZoom(false);
        weightChart.getLegend().setEnabled(false);
    }

    private ArrayList<WeightEntry> getLatestWeightPerDay(ArrayList<WeightEntry> fullHistory) {
        Map<String, WeightEntry> latestPerDay = new HashMap<>();

        for (WeightEntry entry : fullHistory) {
            String dateKey = entry.getDate();
            WeightEntry existing = latestPerDay.get(dateKey);

            // pick the newest log for that date using timestamp
            if (existing == null || entry.getTimestamp() > existing.getTimestamp()) {
                latestPerDay.put(dateKey, entry);
            }
        }

        ArrayList<WeightEntry> list = new ArrayList<>(latestPerDay.values());

        // sort oldest → newest by date so chart timeline is correct
        list.sort((a, b) -> {
            try {
                return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .parse(a.getDate())
                        .compareTo(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .parse(b.getDate()));
            } catch (Exception e) {
                return 0;
            }
        });

        return list;
    }

    private void updateGoalLine(float newGoalWeight) {
        YAxis leftAxis = weightChart.getAxisLeft();

        // Remove old goal line(s)
        leftAxis.removeAllLimitLines();

        // Create new goal line
        LimitLine goalLine = new LimitLine(newGoalWeight, "Goal");
        goalLine.setLineColor(Color.parseColor("#9CCC9C"));
        goalLine.setLineWidth(1.5f);
        goalLine.setTextColor(Color.parseColor("#6FBF73"));
        goalLine.enableDashedLine(10f, 10f, 0f);

        leftAxis.addLimitLine(goalLine);

        // Force redraw
        weightChart.invalidate();
    }
}