package com.example.fitbite;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.fitbite.network.ProxyClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private EditText usernameInput, passwordInput;
    private Button loginButton, createAccountButton, googleSignInButton;
    private TextView toast;
    private View loadingOverlay;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalSettings settings = new LocalSettings(this);
        String theme = settings.getTheme();
        switch (theme) {
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        setContentView(R.layout.login_page); // connects to login_page.xml
        testFatsecret();  // <-- TEST RUNS HERE

        firestore = FirebaseFirestore.getInstance();

        // Connect XML elements
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.create_account_button);
        googleSignInButton = findViewById(R.id.GoogleSignIn);

        // ✅ ADDED LINE
        Button forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        loadingOverlay = getLayoutInflater().inflate(R.layout.loading, null);
        FrameLayout root = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
        root.addView(loadingOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        toast = findViewById(R.id.toast);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("799401116375-mq7i87vusumib87i8194g17cd7t5fcjh.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Handle login button → go to Home
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                loadingScreen(true);
                auth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener(task -> {
                            loadingScreen(false);
                            if (task.isSuccessful()) {
                                showMessage("Login successful!", true);
                                checkUserFirestore();
                            } else {
                                showMessage("Login failed! Invalid Username/Email or Password!", false);
                            }
                        });
            }
        });

        // Handle create account → go to CreateAccount.java
        createAccountButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
            else {
                loadingScreen(true);
                auth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(task -> {
                            loadingScreen(false);
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", user.getEmail());
                                userData.put("uid", user.getUid());
                                userData.put("username", null);
                                firestore.collection("users")
                                        .document(user.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(unused -> {
                                            showMessage("Account created!", true);
                                            startActivity(new Intent(LoginActivity.this, CreateAccount.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e ->
                                                showMessage("Failed to create profile: " + e.getMessage(), false)
                                        );
                            }
                        });
            }
        });

        // ✅ ADDED CLICK LISTENER — takes user to Forgot Password screen
        forgotPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e) {
                showMessage("Google sign-in failed: " + e.getMessage(), false);
            }
        }
    }

    private void checkUserFirestore() {
        String uid = auth.getCurrentUser().getUid();
        DocumentReference userDoc = firestore.collection("users").document(uid);

        userDoc.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Map<String, Object> defaultUser = new HashMap<>();
                defaultUser.put("email", auth.getCurrentUser().getEmail());
                defaultUser.put("uid", auth.getCurrentUser().getUid());
                defaultUser.put("username", auth.getCurrentUser().getDisplayName());

                userDoc.set(defaultUser)
                        .addOnSuccessListener(unused -> {
                            showMessage("User profile created", true);
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e ->
                                showMessage("Failed to create profile: " + e.getMessage(), false)
                        );
            }
        }).addOnFailureListener(e ->
                showMessage("Firestore error: " + e.getMessage(), false)
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            showMessage("No ID token returned", false);
            return;
        }

        loadingScreen(true);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    loadingScreen(false);
                    if (task.isSuccessful()) {
                        showMessage("Google login successful!", true);
                        checkUserFirestore();
                    }
                    else {
                        showMessage("Firebase sign-in failed: " +
                                task.getException().getMessage(), false);
                    }
                });
    }

    private void showMessage(String message, boolean success) {
        toast.setText(message);
        toast.setTextColor(getResources().getColor(
                success ? android.R.color.holo_green_dark : android.R.color.holo_red_dark
        ));
    }

    private void loadingScreen(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }


    private void testFatsecret() {
        new Thread(() -> {
            try {
                ProxyClient proxy = new ProxyClient();
                String json = proxy.searchFood("apple");
                Log.d("FATSECRET_TEST", json);
            } catch (Exception e) {
                Log.e("FATSECRET_TEST", "Error", e);
            }
        }).start();
    }
}
