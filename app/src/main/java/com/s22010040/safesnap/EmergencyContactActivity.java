package com.s22010040.safesnap;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class EmergencyContactActivity extends AppCompatActivity {

    private RecyclerView contactsRecyclerView;
    private ImageButton backBtn;
    private TextView emptyStateMessage;
    private AppCompatButton addNewBtn;
    private ContactsAdapter contactsAdapter;
    private List<Contact> contactsList;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.emergency_contact);

        initializeView();
        setupRecyclerView();
        loadContacts();
        setupClickListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContacts(); // Refresh contacts when returning from add contact screen
    }

    private void initializeView() {

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);
        addNewBtn = findViewById(R.id.addNewBtn);
        backBtn = findViewById(R.id.backBtn);

        sharedPreferences = getSharedPreferences("EmergencyContacts", MODE_PRIVATE);
        gson = new Gson();
        contactsList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        contactsAdapter = new ContactsAdapter(contactsList);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactsAdapter);
    }

    private void loadContacts() {
        String contactsJson = sharedPreferences.getString("contacts_list", "");
        if (!contactsJson.isEmpty()) {
            Type listType = new TypeToken<List<Contact>>(){}.getType();
            contactsList = gson.fromJson(contactsJson, listType);
            if (contactsList == null) {
                contactsList = new ArrayList<>();
            }
        } else {
            contactsList = new ArrayList<>();
        }

        contactsAdapter.updateContacts(contactsList);
        updateEmptyState();
    }

    public void updateEmptyState() {
        if (contactsList.isEmpty()) {
            emptyStateMessage.setVisibility(View.VISIBLE);
            contactsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateMessage.setVisibility(View.GONE);
            contactsRecyclerView.setVisibility(View.VISIBLE);
        }

        // Update button text based on contact count
        if (contactsList.size() >= 5) {
            addNewBtn.setText("Limit Reached (5/5)");
            addNewBtn.setEnabled(false);
            addNewBtn.setAlpha(0.5f);
        } else {
            addNewBtn.setText("+ Add New (" + contactsList.size() + "/5)");
            addNewBtn.setEnabled(true);
            addNewBtn.setAlpha(1.0f);
        }
    }

    private void setupClickListeners() {
        addNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if maximum contacts reached before navigating
                if (contactsList.size() >= 5) {
                    Toast.makeText(EmergencyContactActivity.this,
                            "Maximum 5 emergency contacts allowed", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(EmergencyContactActivity.this, AddEmergencyContactActivity.class);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToDashboard();
            }

            private void navigateToDashboard() {
                Intent intent = new Intent(EmergencyContactActivity.this, dashboard.class);
                startActivity(intent);
                finish();
            }
        });
    }


}
