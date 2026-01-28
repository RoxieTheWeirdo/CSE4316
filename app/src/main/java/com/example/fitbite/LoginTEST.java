/*

From the old tests from previous meetings
Not really needed anymore since it's blended with LoginActivity.java
I don't think it should become an issue, but if this causes problems, feel free to delete

 */
package com.example.fitbite;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginTEST extends AppCompatActivity {

    private static final String TAG = "LoginTEST";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth auth;
    private SignInClient oneTapClient;

    private EditText pressEmail, pressPassword;
    private Button pressRegister, pressEmailLogin, pressGoogleSignIn;
    private TextView toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logintest);
        auth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(this);
        pressEmail = findViewById(R.id.Email);
        pressPassword = findViewById(R.id.Password);
        pressRegister = findViewById(R.id.Register);
        pressEmailLogin = findViewById(R.id.EmailLogin);
        pressGoogleSignIn = findViewById(R.id.GoogleSignIn);
        toast = findViewById(R.id.toast);

        pressRegister.setOnClickListener(v -> registerWithEmail());
        pressEmailLogin.setOnClickListener(v -> loginWithEmail());
        pressGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void registerWithEmail() {
        String email = pressEmail.getText().toString().trim();
        String password = pressPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both email and password", false);
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) showMessage("Account created successfully!", true);
                    else showMessage("Registration failed: " + task.getException().getMessage(), false);
                });
    }

    private void loginWithEmail() {
        String email = pressEmail.getText().toString().trim();
        String password = pressPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both email and password", false);
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) showMessage("Login successful!", true);
                    else showMessage("Login failed: " + task.getException().getMessage(), false);
                });
    }

    private void signInWithGoogle() {
        GetSignInIntentRequest request = GetSignInIntentRequest.builder()
                .setServerClientId("799401116375-mq7i87vusumib87i8194g17cd7t5fcjh.apps.googleusercontent.com")
                .build();

        oneTapClient.getSignInIntent(request)
                .addOnSuccessListener(pendingIntent -> {
                    try {
                        startIntentSenderForResult(
                                pendingIntent.getIntentSender(),
                                RC_SIGN_IN,
                                null,
                                0,
                                0,
                                0
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching sign-in intent", e);
                        showMessage("Sign-in intent error: " + e.getMessage(), false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Google Sign-In Intent failed", e);
                    showMessage("Google Sign-In failed: " + e.getMessage(), false);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();

                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken);
                } else {
                    showMessage("No ID token returned", false);
                }
            } catch (ApiException e) {
                if (e.getStatusCode() == CommonStatusCodes.CANCELED) {
                    showMessage("Sign-in canceled", false);
                } else {
                    showMessage("Google sign-in failed: " + e.getMessage(), false);
                }
                Log.e(TAG, "onActivityResult: Sign-in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        showMessage("Google login successful!", true);
                    } else {
                        showMessage("Firebase sign-in failed: " + task.getException().getMessage(), false);
                    }
                });
    }

    private void showMessage(String message, boolean success) {
        toast.setText(message);
        toast.setTextColor(getResources().getColor(success
                ? android.R.color.holo_green_dark
                : android.R.color.holo_red_dark));
    }
}
