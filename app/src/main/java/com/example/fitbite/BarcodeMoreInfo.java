package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;


public class BarcodeMoreInfo extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_more_info);

        TextView servingSizeView = (TextView) findViewById(R.id.servingSizeTextView);
        String servingSize = "1";
        servingSizeView.setText("Serving Size: " + servingSize);

        TextView fatsSizeView = (TextView) findViewById(R.id.fatsTextView);
        String saturated = "0g";
        String trans = "0g";
        String poly = "0g";
        String mono = "0g";
        fatsSizeView.setText("Saturated Fat: " + saturated +
                             "\nTrans Fat: " + trans +
                             "\nPolyunsaturated Fat: " + poly +
                             "\nMonounsaturated Fat: " + mono);

        TextView cholView = (TextView) findViewById(R.id.cholTextView);
        String cholesterol = "0mg";
        cholView.setText("Cholesterol: " + cholesterol);

        TextView sodiumView = (TextView) findViewById(R.id.sodiumTextView);
        String sodium = "0mg";
        sodiumView.setText("Sodium: " + sodium);

        TextView carbsView = (TextView) findViewById(R.id.carbsTextView);
        String fiber = "0g";
        String sugars = "0g";
        String addedSugars = "0g";
        carbsView.setText("Dietary Fiber: " + fiber +
                          "\nSugars: " + sugars +
                          "\nIncludes " + addedSugars + " Added Sugars");

        TextView proteinView = (TextView) findViewById(R.id.proteinTextView);
        String protein = "0g";
        proteinView.setText("Protein: " + protein);

        TextView nutrientView = (TextView) findViewById(R.id.nutrientsTextView);
        String vitaminD = "0mcg";
        String calcium = "0mcg";
        String iron = "0mcg";
        String potassium = "0mcg";
        String vitaminA = "0mcg";
        nutrientView.setText("Vitamin D: " + vitaminD +
                             "\nCalcium: " + calcium +
                             "\nIron: " + iron +
                             "\nPotassium: " + potassium +
                             "\nVitamin A: " + vitaminA);



    }

}
