package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class activity_basics_sex extends AppCompatActivity {

    private CardView cardFemale, cardMale;
    private RadioButton radioFemale, radioMale;
    private Button btnNext;
    private String selectedSex = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_sex);

        cardFemale = findViewById(R.id.cardFemale);
        cardMale = findViewById(R.id.cardMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioMale = findViewById(R.id.radioMale);
        btnNext = findViewById(R.id.btnNext);

        btnNext.setEnabled(false);

        cardFemale.setOnClickListener(v -> selectSex("Female"));
        cardMale.setOnClickListener(v -> selectSex("Male"));
        radioFemale.setOnClickListener(v -> selectSex("Female"));
        radioMale.setOnClickListener(v -> selectSex("Male"));

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(activity_basics_sex.this, activity_basics_birthday.class);
            intent.putExtra("sex", selectedSex);
            startActivity(intent);
        });
    }

    private void selectSex(String sex) {
        selectedSex = sex;
        radioFemale.setChecked(sex.equals("Female"));
        radioMale.setChecked(sex.equals("Male"));
        btnNext.setEnabled(true);
        btnNext.setBackgroundResource(R.drawable.button_enabled_background);
        btnNext.setTextColor(getResources().getColor(android.R.color.black));
    }
}
