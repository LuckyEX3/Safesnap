package com.s22010040.safesnap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class status1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_status1);

        initializeViews();

    }
    private void initializeViews()

          {Button nextButton = findViewById(R.id.nextButton);
              nextButton.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent intent = new Intent(status1.this, status2.class);
                     startActivity(intent);
            }
        });

    }
}
