package com.s22010040.safesnap;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SettingActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText;
    private AppCompatButton saveDetailsBtn, cancelBtn, changePasswordBtn, signOutBtn;
    private ImageButton backBtn;
    private SharedPreferences sharedPreferences;
    private DatabaseHelper databaseHelper;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private String currentUserEmail; // Store the current user's email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        databaseHelper = new DatabaseHelper(this);

        initializeViews();

        // ✅ Load user data from database (corrected)
        loadUserDataFromDatabase();

        setClickListeners();
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        saveDetailsBtn = findViewById(R.id.saveDetailsBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        signOutBtn = findViewById(R.id.signOut);
        backBtn = findViewById(R.id.backBtn);
    }

    private void loadUserDataFromDatabase() {
        SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        currentUserEmail = loginPrefs.getString("user_email", "");

        if (!currentUserEmail.isEmpty()) {
            Cursor cursor = databaseHelper.getUserDetails(currentUserEmail);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("USERNAME"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("EMAIL"));

                nameEditText.setText(name);
                emailEditText.setText(email);
                cursor.close();
            } else {
                // Fallback if DB fails
                Toast.makeText(this, "No data found in database. Trying SharedPreferences...", Toast.LENGTH_SHORT).show();
                loadUserData();
            }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_LONG).show();
        }
    }

    private void loadUserData() {
        String savedName = sharedPreferences.getString(KEY_NAME, "");
        String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
        nameEditText.setText(savedName);
        emailEditText.setText(savedEmail);
    }

    private void setClickListeners() {
        backBtn.setOnClickListener(v -> onBackPressed());

        saveDetailsBtn.setOnClickListener(v -> saveUserDetails());

        cancelBtn.setOnClickListener(v -> {
            loadUserDataFromDatabase();
            Toast.makeText(this, "Changes cancelled", Toast.LENGTH_SHORT).show();
        });

        changePasswordBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        signOutBtn.setOnClickListener(v -> signOut());
    }

    private void saveUserDetails() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        // If changing email, check if the new email already exists
        if (!email.equals(currentUserEmail) && databaseHelper.checkEmail(email)) {
            emailEditText.setError("Email already exists");
            emailEditText.requestFocus();
            return;
        }

        boolean isUpdated = databaseHelper.updateUserDetails(currentUserEmail, name, email);

        if (isUpdated) {
            // Update SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_NAME, name);
            editor.putString(KEY_EMAIL, email);
            editor.apply();

            // Update login_prefs
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            SharedPreferences.Editor loginEditor = loginPrefs.edit();
            loginEditor.putString("user_name", name);
            loginEditor.putString("user_email", email);
            loginEditor.apply();

            currentUserEmail = email;

            Toast.makeText(this, "Details saved successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update details", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void signOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        SharedPreferences.Editor loginEditor = loginPrefs.edit();
        loginEditor.putBoolean("isLoggedIn", false);
        loginEditor.remove("user_email");
        loginEditor.remove("user_name");
        loginEditor.apply();

        Intent intent = new Intent(SettingActivity.this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
    }
}
