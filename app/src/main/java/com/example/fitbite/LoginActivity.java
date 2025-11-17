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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page); // connects to login_page.xml

        // Connect XML elements
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.create_account_button);
        //The buttons from LoginTEST.java, added a Google SignIn button but we can refine that later
        googleSignInButton = findViewById(R.id.GoogleSignIn);

        loadingOverlay = getLayoutInflater().inflate(R.layout.loading, null);
        FrameLayout root = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
        root.addView(loadingOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        toast = findViewById(R.id.toast);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
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
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                showMessage("Login failed! Invalid Username/Email or Password!", false);
                            }
                        });
            }
        });

        // Handle create account → go to CreateAccount.java
        createAccountButton.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingScreen(true);
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                // Create a new user with a first and last name
                                Map<String, Object> userDoc = new HashMap<>();
                                userDoc.put("uid", user.getUid());
                                userDoc.put("email", user.getEmail());

                                // Add a new document with a generated ID
                                db.collection("users").document(user.getUid())
                                        .set(userDoc)
                                        .addOnSuccessListener(aVoid -> {
                                            loadingScreen(false);
                                            Log.d(TAG, "User successfully written!");
                                            Intent intent = new Intent(LoginActivity.this, CreateAccount.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            loadingScreen(false);
                                            Log.w(TAG, "Error writing document", e);
                                            showMessage("Error saving user data.", false);
                                        });

                            }
                        } else {
                            loadingScreen(false);
                            showMessage("Authentication failed: " + task.getException().getMessage(), false);
                        }
                    });
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
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e) {
                showMessage("Google sign-in failed: " + e.getMessage(), false);
            }
        }
    }
//Currently, if you sign in with Google it bypasses the create account
//When we implement a database, if user data is null, then we can take it to the CreateAccount.java, where fields will be filled in appropriately.
//Think of this as more of a placeholder for right now
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
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        showMessage("Firebase sign-in failed: " + task.getException().getMessage(), false);
                    }
                });
    }

    //Sometimes the text is a bit too fast for the toast, so I put this here just to help me a little bit extra
    private void showMessage(String message, boolean success) {
        toast.setText(message);
        toast.setTextColor(getResources().getColor(success
                ? android.R.color.holo_green_dark
                : android.R.color.holo_red_dark));
    }
    private void loadingScreen(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
