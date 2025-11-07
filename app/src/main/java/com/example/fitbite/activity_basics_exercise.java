package com.example.fitbite;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class activity_basics_exercise extends AppCompatActivity {

    private CardView card0Sessions, card1to3Sessions, card4to6Sessions, card7PlusSessions;
    private RadioButton radio0Sessions, radio1to3Sessions, radio4to6Sessions, radio7PlusSessions;
    private Button btnNext;
    private String selectedExercise = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_exercise);

        // Initialize views
        card0Sessions = findViewById(R.id.card0Sessions);
        card1to3Sessions = findViewById(R.id.card1to3Sessions);
        card4to6Sessions = findViewById(R.id.card4to6Sessions);
        card7PlusSessions = findViewById(R.id.card7PlusSessions);

        radio0Sessions = findViewById(R.id.radio0Sessions);
        radio1to3Sessions = findViewById(R.id.radio1to3Sessions);
        radio4to6Sessions = findViewById(R.id.radio4to6Sessions);
        radio7PlusSessions = findViewById(R.id.radio7PlusSessions);

        btnNext = findViewById(R.id.btnNext);

        // 0 sessions card
        card0Sessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectExercise(0);
            }
        });

        // 1-3 sessions card
        card1to3Sessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectExercise(1);
            }
        });

        // 4-6 sessions card
        card4to6Sessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectExercise(2);
            }
        });

        // 7+ sessions card
        card7PlusSessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectExercise(3);
            }
        });

        // Next button
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace 'MainActivity' with your next activity
                Intent intent = new Intent(activity_basics_exercise.this, MainActivity.class);
                intent.putExtra("exercise", selectedExercise);
                startActivity(intent);
                finish(); // Close the onboarding flow
            }
        });
    }

    private void selectExercise(int option) {
        // Uncheck all
        radio0Sessions.setChecked(false);
        radio1to3Sessions.setChecked(false);
        radio4to6Sessions.setChecked(false);
        radio7PlusSessions.setChecked(false);

        // Check selected
        switch(option) {
            case 0:
                radio0Sessions.setChecked(true);
                selectedExercise = "0 sessions/week";
                break;
            case 1:
                radio1to3Sessions.setChecked(true);
                selectedExercise = "1-3 sessions/week";
                break;
            case 2:
                radio4to6Sessions.setChecked(true);
                selectedExercise = "4-6 sessions/week";
                break;
            case 3:
                radio7PlusSessions.setChecked(true);
                selectedExercise = "7+ sessions/week";
                break;
        }

        enableNextButton();
    }

    private void enableNextButton() {
        btnNext.setEnabled(true);
        btnNext.setBackgroundResource(R.drawable.button_enabled_background);
        btnNext.setTextColor(getResources().getColor(android.R.color.black));
    }
}