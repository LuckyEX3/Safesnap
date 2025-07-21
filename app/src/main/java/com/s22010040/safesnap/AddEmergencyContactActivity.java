package com.s22010040.safesnap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AddEmergencyContactActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText nameEditText, phoneEditText;
    private AppCompatButton saveContactBtn, cancelBtn;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_emergency_contact);

        initializeView();
        setupClickListeners();
    }

    private void setupClickListeners() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToEmergencyContact();
            }
        });

        saveContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToEmergencyContact();
            }
        });
    }

    private void saveContact(){
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (name.isEmpty()){
            Toast.makeText(this, "Please enter contact name" , Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.isEmpty()){
            Toast.makeText(this, "Please enter phone number" , Toast.LENGTH_SHORT).show();
            return;
        }

        //get existing contacts
        List<Contact> contacts = getContactFromSharedPreferences();

        // check if maximum limit reached
        if (contacts.size() >= 5){
            Toast.makeText(this , "Maximum 5 emergency contacts allowed", Toast.LENGTH_LONG).show();
            return;
        }

        // add new contact
        contacts.add(new Contact(name, phone));

        // save updated list
        saveContactsToSharedPreferences(contacts);
        Toast.makeText(this, "Contact saved successfully", Toast.LENGTH_SHORT).show();
        navigateToEmergencyContact();
    }

    private List<Contact> getContactFromSharedPreferences() {
        // Use the same SharedPreferences name as EmergencyContactActivity
        SharedPreferences sharedPreferences = getSharedPreferences("EmergencyContacts", MODE_PRIVATE);
        String contactsJson = sharedPreferences.getString("contacts_list", "[]");

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Contact>>(){}.getType();
        List<Contact> contacts = gson.fromJson(contactsJson, listType);

        return contacts != null ? contacts : new ArrayList<>();
    }

    private void saveContactsToSharedPreferences(List<Contact> contacts) {
        String contactsJson = gson.toJson(contacts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("contacts_list", contactsJson);
        editor.apply();
    }

    private void navigateToEmergencyContact() {
        Intent intent = new Intent(AddEmergencyContactActivity.this, EmergencyContactActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeView() {
        backBtn = findViewById(R.id.backBtn);
        // FIX: Assign correct EditTexts
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText); // This should be phoneEditText, not nameEditText
        saveContactBtn = findViewById(R.id.saveContactBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        sharedPreferences = getSharedPreferences("EmergencyContacts", MODE_PRIVATE);
        gson = new Gson();
    }
}