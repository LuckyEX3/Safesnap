package com.s22010040.safesnap;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class EmergencyContactActivity extends AppCompatActivity {

    private Button addNewBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.emergency_contact);

        initializeView();

    }

    private void initializeView() {

        addNewBtn = findViewById(R.id.addNewBtn);

        addNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmergencyContactActivity.this, AddEmergencyContactActivity.class);
                startActivity(intent);
            }
        });
    }
}
