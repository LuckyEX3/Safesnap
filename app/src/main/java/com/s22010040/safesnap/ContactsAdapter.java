package com.s22010040.safesnap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private List<Contact> contacts;
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public ContactsAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        sharedPreferences = context.getSharedPreferences("EmergencyContacts", Context.MODE_PRIVATE);
        gson = new Gson();

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.nameTextView.setText(contact.getName());
        holder.phoneTextView.setText(contact.getPhoneNumber());

        // Set up delete button click listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(holder.getAdapterPosition(), contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private void showDeleteConfirmationDialog(int position, Contact contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Contact");
        builder.setMessage("Are you sure you want to delete " + contact.getName() + "?");

        // Delete button
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteContact(position);
                dialog.dismiss();
            }
        });

        // Cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Customize button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#E53E3E"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#666666"));
    }

    private void deleteContact(int position) {
        // Remove contact from list
        contacts.remove(position);

        // Update RecyclerView
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, contacts.size());

        // Save updated list to SharedPreferences
        saveContactsToSharedPreferences(contacts);

        // Show success message
        Toast.makeText(context, "Contact deleted successfully", Toast.LENGTH_SHORT).show();

        // Update empty state if needed
        if (context instanceof EmergencyContactActivity) {
            ((EmergencyContactActivity) context).updateEmptyState();
        }
    }

    private void saveContactsToSharedPreferences(List<Contact> contacts) {
        String contactsJson = gson.toJson(contacts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("contacts_list", contactsJson);
        editor.apply();
    }

    public void updateContacts(List<Contact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        TextView deleteButton;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contactName);
            phoneTextView = itemView.findViewById(R.id.contactPhone);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}