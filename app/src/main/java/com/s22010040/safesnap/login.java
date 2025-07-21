package com.s22010040.safesnap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class login extends AppCompatActivity {

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Go directly to dashboard
            startActivity(new Intent(login.this, dashboard.class));
            finish(); // Prevent back navigation to login
            return;
        }

       initializeView();


    }

    private void initializeView() {
        TextView tvSignup = findViewById(R.id.signupLink);

        SpannableString ss = new SpannableString("Don't have an account? Sign Up");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(login.this, signup.class);
                startActivity(intent);
            }
        };

        ss.setSpan(clickableSpan, 23, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD), 23, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvSignup.setText(ss);
        tvSignup.setMovementMethod(LinkMovementMethod.getInstance());

        db = new DatabaseHelper(this);

        Button loginBtn = findViewById(R.id.loginBtn);
        EditText editEmail = findViewById(R.id.editEmail);
        EditText editPassword = findViewById(R.id.editPassword);

        loginBtn.setOnClickListener(v -> {
            String user = editEmail.getText().toString();
            String pass = editPassword.getText().toString();

            if (db.validateUser(user, pass)) {
                SharedPreferences sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("user_email", user);
                editor.apply();

                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(login.this, dashboard.class));
                finish(); // Close login screen so user can’t go back
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        
    }
}
