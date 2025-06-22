package com.s22010040.safesnap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class signup extends AppCompatActivity {

    EditText editUsername, editEmail, editPassword, editConfirmPassword;
    Button signupBtn;
    DatabaseHelper db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        editUsername = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        signupBtn = findViewById(R.id.signupBtn);

        db = new DatabaseHelper(this);

        signupBtn.setOnClickListener(v -> {
            String user = editUsername.getText().toString();
            String email = editEmail.getText().toString();
            String password = editPassword.getText().toString();
            String confirmPassword = editConfirmPassword.getText().toString();


            if (user.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.checkEmail(email)) {
                Toast.makeText(this, "User already exists. Please login.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = db.insertData(user, email, password);
            if (inserted) {
                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, login.class));
                finish();
            } else {
                Toast.makeText(this, "Signup failed. Try again.", Toast.LENGTH_SHORT).show();
            }

        });

    }
}
