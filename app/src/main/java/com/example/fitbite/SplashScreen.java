package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        statusText = findViewById(R.id.statusText);
        new Handler().postDelayed(this::checkLoginStatus, 1000);
    }

    private void checkLoginStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            statusText.setText("Signing you in...");
            if (user != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .update("lastActive", com.google.firebase.Timestamp.now());
            }
            startActivity(new Intent(SplashScreen.this, HomeActivity.class));
        }
        else {
            startActivity(new Intent(SplashScreen.this, LoginActivity.class));
        }
        finish();
    }
}