package com.s22010040.safesnap;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuickCallActivity extends AppCompatActivity {

    private RecyclerView contactsRecyclerView;
    private TextView emptyStateMessage;
    private ImageButton backBtn;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private List<Contact> contactsList;
    private QuickCallAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quickcall);

        // Request CALL_PHONE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 1);
        }

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);
        emptyStateMessage = findViewById(R.id.emptyStateMessage);
        backBtn = findViewById(R.id.backBtn);

        sharedPreferences = getSharedPreferences("EmergencyContacts", Context.MODE_PRIVATE);
        gson = new Gson();

        loadContacts();

        backBtn.setOnClickListener(v -> {
            finish(); // or navigate back to Dashboard
        });
    }

    private void loadContacts() {
        String contactsJson = sharedPreferences.getString("contacts_list", "");
        Type listType = new TypeToken<List<Contact>>() {}.getType();
        contactsList = gson.fromJson(contactsJson, listType);

        if (contactsList == null || contactsList.isEmpty()) {
            contactsList = new ArrayList<>();
            emptyStateMessage.setVisibility(View.VISIBLE);
            contactsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateMessage.setVisibility(View.GONE);
            contactsRecyclerView.setVisibility(View.VISIBLE);
        }

        adapter = new QuickCallAdapter(contactsList, this);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(adapter);
    }
}
