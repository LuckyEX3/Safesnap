package com.s22010040.safesnap;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Looper;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogRecord;

public class SosActivatedActivity extends AppCompatActivity {

    private TextView timerText, sosAlertText, timeLabel, alertStatusText, locationStatusText;
    private Button callBtn, cancelBtn;
    private ImageView emergencyIcon, timerIcon;
    private LinearLayout topBar, timerSection, actionButtons, statusSection;
    private Handler timerHandler; // Remove duplicate - keep only this one
    private long startTime;
    private boolean isActive = true;
    private int emergencyContactsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sos_activatednew);


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show confirmation dialog
                new AlertDialog.Builder(SosActivatedActivity.this)
                        .setTitle("Emergency Alert Active")
                        .setMessage("Emergency alert is still active. Do you want to cancel it?")
                        .setPositiveButton("Cancel Alert", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelAlert();
                            }
                        })
                        .setNegativeButton("Keep Active", null)
                        .show();
            }
        });

        initializeViews();
        setupUIElements();
        startTimer();
        sendEmergencyAlert();

        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    private void initializeViews() {
        // Initialize all UI elements from XML
        timerText = findViewById(R.id.timerText);
        sosAlertText = findViewById(R.id.sosAlertText);
        timeLabel = findViewById(R.id.textView3);
        callBtn = findViewById(R.id.callBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        emergencyIcon = findViewById(R.id.emergencyIcon);
        timerIcon = findViewById(R.id.imageView);
        topBar = findViewById(R.id.topBar);
        statusSection = findViewById(R.id.linearLayout);

        // Get references to status text views within the status section
        if (statusSection != null && statusSection.getChildCount() >= 2) {
            alertStatusText = (TextView) statusSection.getChildAt(0);
            locationStatusText = (TextView) statusSection.getChildAt(1);
        }

        // Set up button click listeners
        setupButtonListeners();

        // Initialize timer components - CORRECTED
        timerHandler = new Handler(Looper.getMainLooper());
        startTime = System.currentTimeMillis();
    }



    private void setupButtonListeners() {
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call119(); // Updated to call 119 as per XML
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlert();
            }
        });
    }

    private void setupUIElements() {
        // Set up the SOS alert text
        if (sosAlertText != null) {
            sosAlertText.setText("SOS ALERT SENT");
            sosAlertText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }

        // Set up the timer label
        if (timeLabel != null) {
            timeLabel.setText("Time since alert");
            timeLabel.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }

        // Set up emergency icon if needed
        setupEmergencyIcon();

        // Set up timer icon if needed
        setupTimerIcon();

        // Update call button text to match XML
        if (callBtn != null) {
            callBtn.setText("📞 Call 119");
        }

        // Update cancel button text to match XML
        if (cancelBtn != null) {
            cancelBtn.setText("✓ I'm Safe - Cancel Alert");
        }
    }

    private void setupEmergencyIcon() {
        if (emergencyIcon != null) {
            emergencyIcon.setImageResource(R.drawable.siren);
            emergencyIcon.setContentDescription("Emergency siren icon");
        }
    }

    private void setupTimerIcon() {
        if (timerIcon != null) {

            timerIcon.setImageResource(R.drawable.timer);
            timerIcon.setContentDescription("Timer icon");
        }
    }

    private void startTimer() {
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isActive) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int minutes = (int) (elapsed / 60000);
                    int seconds = (int) ((elapsed % 60000) / 1000);

                    String timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                    if (timerText != null) {
                        timerText.setText(timeString);
                    }

                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void sendEmergencyAlert() {
        // Send emergency alert to contacts
        sendSMSToEmergencyContacts();
        shareLocationWithContacts();

        // Update UI to show alert status
        updateAlertStatus();

        // Show confirmation toast
        Toast.makeText(this, "Emergency alert sent to contacts", Toast.LENGTH_LONG).show();
    }

    private void updateAlertStatus() {
        // Update the status text in the UI
        emergencyContactsCount = getEmergencyContacts().size();

        if (alertStatusText != null) {
            String statusText = "Emergency alert sent to " + emergencyContactsCount + " contacts";
            alertStatusText.setText(statusText);
            alertStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        }

        if (locationStatusText != null) {
            locationStatusText.setText("Location shared automatically");
            locationStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        }
    }

    private void sendSMSToEmergencyContacts() {
        // Get emergency contacts from SharedPreferences or database
        List<String> emergencyContacts = getEmergencyContacts();

        String message = "EMERGENCY ALERT: I need help! This is an automated message from my safety app. " +
                "My current location: " + getCurrentLocation();

        SmsManager smsManager = SmsManager.getDefault();

        for (String contact : emergencyContacts) {
            try {
                smsManager.sendTextMessage(contact, null, message, null, null);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("SOS", "Failed to send SMS to " + contact, e);
            }
        }
    }

    private void shareLocationWithContacts() {
        // Implement location sharing logic
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Get last known location or request current location
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                String locationUrl = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                // Send location URL to emergency contacts
                sendLocationSMS(locationUrl);
            } else {
                // Request fresh location
                requestCurrentLocation();
            }
        } else {
            // Handle permission not granted
            updateLocationStatus("Location permission not available");
        }
    }

    private void requestCurrentLocation() {
        // Request current location if last known location is not available
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    String locationUrl = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                    sendLocationSMS(locationUrl);
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };

            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    private void updateLocationStatus(String status) {
        if (locationStatusText != null) {
            locationStatusText.setText(status);
        }
    }

    private void sendLocationSMS(String locationUrl) {
        List<String> emergencyContacts = getEmergencyContacts();
        String locationMessage = "My current location: " + locationUrl;

        SmsManager smsManager = SmsManager.getDefault();

        for (String contact : emergencyContacts) {
            try {
                smsManager.sendTextMessage(contact, null, locationMessage, null, null);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("SOS", "Failed to send location SMS to " + contact, e);
            }
        }
    }

    private List<String> getEmergencyContacts() {
        List<String> contacts = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("emergency_contacts", MODE_PRIVATE);
        String contact1 = prefs.getString("contact_1", null);
        String contact2 = prefs.getString("contact_2", null);
        String contact3 = prefs.getString("contact_3", null);
        String contact4 = prefs.getString("contact_4", null);

        if (contact1 != null && !contact1.trim().isEmpty()) contacts.add(contact1);
        if (contact2 != null && !contact2.trim().isEmpty()) contacts.add(contact2);
        if (contact3 != null && !contact3.trim().isEmpty()) contacts.add(contact3);
        if (contact4 != null && !contact4.trim().isEmpty()) contacts.add(contact4);

        return contacts;
    }

    private String getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                return "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude();
            }
        }

        return "Location being determined...";
    }

    private void call119() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:119")); // Updated to 119 as per XML

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                startActivity(callIntent);
            } catch (ActivityNotFoundException e) {
                // Fall back to dial if call fails
                dialEmergencyNumber();
            }
        } else {
            // Use ACTION_DIAL if permission not granted
            dialEmergencyNumber();
        }
    }

    private void dialEmergencyNumber() {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:119"));
        try {
            startActivity(dialIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Unable to make emergency call", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlert() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Cancel Emergency Alert")
                .setMessage("Are you sure you want to cancel the emergency alert?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send cancellation message to contacts
                        sendCancellationMessage();

                        // Update UI to show cancelled status
                        updateCancelledStatus();

                        // Close activity and return to dashboard
                        isActive = false;

                        // Delay finish to allow user to see the cancellation message
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 2000);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateCancelledStatus() {
        if (sosAlertText != null) {
            sosAlertText.setText("ALERT CANCELLED");
            sosAlertText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }

        if (alertStatusText != null) {
            alertStatusText.setText("Cancellation message sent to " + emergencyContactsCount + " contacts");
            alertStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }

        if (locationStatusText != null) {
            locationStatusText.setText("Emergency alert has been cancelled");
            locationStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
    }

    private void sendCancellationMessage() {
        List<String> emergencyContacts = getEmergencyContacts();
        String cancelMessage = "EMERGENCY ALERT CANCELLED: I am safe now. Please disregard the previous emergency alert.";

        SmsManager smsManager = SmsManager.getDefault();

        for (String contact : emergencyContacts) {
            try {
                smsManager.sendTextMessage(contact, null, cancelMessage, null, null);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("SOS", "Failed to send cancellation SMS to " + contact, e);
            }
        }

        Toast.makeText(this, "Cancellation message sent to contacts", Toast.LENGTH_LONG).show();
    }

    private void animateEmergencyIcon() {
        if (emergencyIcon != null) {
            // Add pulsing animation to emergency icon
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(emergencyIcon, "scaleX", 1.0f, 1.2f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(emergencyIcon, "scaleY", 1.0f, 1.2f, 1.0f);

            // Set repeat count on individual animators
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(1000);
            animatorSet.start();
        }
    }

    private void setupStatusBar() {
        // Set status bar color to match emergency theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        // Start emergency icon animation when activity resumes
//        animateEmergencyIcon();
//        setupStatusBar();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
        if (timerHandler != null) {
            timerHandler.removeCallbacksAndMessages(null);
        }
    }



}
