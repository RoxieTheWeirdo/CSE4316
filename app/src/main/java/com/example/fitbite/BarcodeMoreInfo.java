package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class BarcodeMoreInfo extends AppCompatActivity {
    private TextView proteinView, cholView, fiberView, sugarView, servingSizeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_more_info);

        ArrayList<String> list = getIntent().getStringArrayListExtra("data");

        servingSizeView = findViewById(R.id.servingSizeTextView);
        cholView = findViewById(R.id.cholTextView);
        fiberView = findViewById(R.id.fiberTextView);
        sugarView = findViewById(R.id.sugarTextView);
        proteinView = findViewById(R.id.proteinTextView);

        servingSizeView.setText(list.get(0));
        cholView.setText(list.get(1) + "mg");
        fiberView.setText(list.get(2) + "g");
        sugarView.setText(list.get(3) + "g");
        proteinView.setText(list.get(4) + "g");
    }
}