package com.s22010040.safesnap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        initializeView();

    }

    private void initializeView() {

        //contact button

        Button contactsBtn = findViewById(R.id.contactsBtn);
        contactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(dashboard.this, EmergencyContactActivity.class);
                startActivity(intent);
            }
        });

        //Share location
    }
}
