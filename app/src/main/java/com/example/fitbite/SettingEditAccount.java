package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
public class SettingEditAccount extends AppCompatActivity {
    private View loadingOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Button ChangeUsername = findViewById(R.id.ChangeUsername);
        Button ChangeEmail = findViewById(R.id.ChangeEmail);
        Button ChangePassword = findViewById(R.id.ChangePassword);
        Button DeleteAccount = findViewById(R.id.DeleteAccount);
        TextView curEmail = findViewById(R.id.curEmail);
        TextView curUser = findViewById(R.id.curUsername);
        if (user != null) {
            // Set email
            curEmail.setText(user.getEmail());

            // Firestore listener for username
            DocumentReference userDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid());

            userDoc.addSnapshotListener((snapshot, e) -> {
                if (e != null) return;
                if (snapshot != null && snapshot.exists()) {
                    String username = snapshot.getString("username");
                    curUser.setText(username != null ? username : "Unknown");
                }
            });
        }
        loadingOverlay = getLayoutInflater().inflate(R.layout.loading, null);
        FrameLayout root = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
        root.addView(loadingOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        ChangeEmail.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.settings_changeemail, null);
            EditText editEmail = dialogView.findViewById(R.id.editEmail);
            Button submitEmail = dialogView.findViewById(R.id.submitEmail);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            submitEmail.setOnClickListener(view -> {
                String newEmail = editEmail.getText().toString().trim();
                if (newEmail.isEmpty()) {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadingScreen(true);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser(); // fresh user
                if (currentUser != null) {
                    currentUser.verifyBeforeUpdateEmail(newEmail)
                            .addOnCompleteListener(task -> {
                                loadingScreen(false);
                                if (task.isSuccessful()) {
                                    dialog.dismiss();
                                    showEmailVerificationOverlay();
                                }
                                else {
                                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    loadingScreen(false);
                    Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        });
        ChangePassword.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.settings_changepassword, null);
            EditText editPassword1 = dialogView.findViewById(R.id.editPassword1);
            EditText editPassword2 = dialogView.findViewById(R.id.editPassword2);
            Button submitPassword = dialogView.findViewById(R.id.submitPassword);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            submitPassword.setOnClickListener(view -> {
                String pass1 = editPassword1.getText().toString();
                String pass2 = editPassword2.getText().toString();
                if (pass1.length() <= 8) {
                    Toast.makeText(this, "Password must be greater than 8 characters!", Toast.LENGTH_SHORT).show();
                }
                else if(!pass1.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:'\",.<>/?`~].*")) {
                    Toast.makeText(this, "Password must contain at least one special character!", Toast.LENGTH_SHORT).show();
                }
                else if(pass1.isEmpty() || pass2.isEmpty()){
                    Toast.makeText(this, "Please fill in both fields!", Toast.LENGTH_SHORT).show();
                }
                else if(!pass1.equals(pass2)){
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (user != null) {
                        user.updatePassword(pass1)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else {
                        Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            });

            dialog.show();
        });
        DeleteAccount.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.settings_deleteaccount, null);
            EditText deletePasswordInput = dialogView.findViewById(R.id.deletePasswordInput);
            Button deleteSubmit = dialogView.findViewById(R.id.deleteSubmit);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            deleteSubmit.setOnClickListener(view -> {
                String password = deletePasswordInput.getText().toString().trim();

                if (password.isEmpty()) {
                    Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                deleteAccountWithPassword(password, dialog);
            });
            dialog.show();
        });
        ChangeUsername.setOnClickListener(v -> {
            // Inflate a small dialog layout
            View dialogView = LayoutInflater.from(this).inflate(R.layout.settings_changeusername, null);
            EditText editUsername = dialogView.findViewById(R.id.editUsername);
            Button submitUsername = dialogView.findViewById(R.id.submitUsername);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            // Optional: pre-fill current username
            FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            editUsername.setText(doc.getString("username"));
                        }
                    });

            submitUsername.setOnClickListener(view -> {
                String newUsername = editUsername.getText().toString().trim();

                if (newUsername.isEmpty()) {
                    Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("username", newUsername)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed to update username: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            });

            dialog.show();
        });

    }
    private void deleteAccountWithPassword(String password, AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        if (email == null) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Re-authenticate
        AuthCredential credential =
                EmailAuthProvider.getCredential(email, password);

        user.reauthenticate(credential).addOnCompleteListener(authTask -> {
            if (!authTask.isSuccessful()) {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Delete Firestore document
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .delete()
                    .addOnCompleteListener(fireTask -> {
                        // 3. Delete Firebase Auth account
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                dialog.dismiss();
                                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

                                // 4. Go back to login
                                Intent intent = new Intent(SettingEditAccount.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Toast.makeText(this, "Delete failed: " +
                                        deleteTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    });
        });
    }
    private void showEmailVerificationOverlay() {
        View overlayView = LayoutInflater.from(this).inflate(R.layout.emailverify, null);

        Dialog overlayDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        overlayDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        overlayDialog.setContentView(overlayView);
        overlayDialog.setCanceledOnTouchOutside(true);

        Window window = overlayDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setGravity(Gravity.CENTER);
        }

        FrameLayout rootLayout = overlayView.findViewById(R.id.rootLayout);
        rootLayout.setOnClickListener(v -> overlayDialog.dismiss());

        overlayDialog.show();
    }
    private void loadingScreen(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}