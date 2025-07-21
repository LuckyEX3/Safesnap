package com.s22010040.safesnap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class dashboard extends AppCompatActivity implements SensorEventListener {

    private TextView sosBtn;
    private Handler sosHandler;
    private Runnable sosRunnable;
    private boolean isSosPresses = false;

    //shake detection variables
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;
    private static final int SHAKE_THRESHOLD = 600;
    private static final int TIME_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        TextView greetingText = findViewById(R.id.greetingText);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("user_name", "User"); // Default fallback

        greetingText.setText("Hi " + userName);

        initializeView();
        initializeShakeDetection();
    }

    private void initializeView() {
        //initialize sos button
        sosBtn = findViewById(R.id.sosBtn);
        sosHandler = new Handler(Looper.getMainLooper());

        // setup sos button press & hold
        setupSosButton();

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
        Button shareLocationBtn = findViewById(R.id.shareLocationBtn);
        shareLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(dashboard.this, MapActivity.class);
                startActivity(intent);
            }
        });

        // setting
        Button settingBtn = findViewById(R.id.settingsBtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(dashboard.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupSosButton() {
        sosBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isSosPresses = true;
                        startSosCountdown();
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isSosPresses = false;
                        cancelSosCountdown();
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100);
                        return true;
                }
                return false;
            }
        });
    }

    private void startSosCountdown(){
        sosRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSosPresses){
                    activateSos();
                }
            }
        };
        sosHandler.postDelayed(sosRunnable, 3000);
    }

    private void cancelSosCountdown(){
        if (sosRunnable != null){
            sosHandler.removeCallbacks(sosRunnable);
        }
    }

    private void initializeShakeDetection() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null){
                // FIXED: Pass 'this' as the listener, not 'this.accelerometer'
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastUpdate) > TIME_THRESHOLD){
                long diffTime = (currentTime - lastUpdate);
                lastUpdate = currentTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) /diffTime * 10000;

                if (speed > SHAKE_THRESHOLD){
                    activateSos();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    private void activateSos(){
        if (isFinishing() || isDestroyed()){
            return;
        }

        // add haptic feedback
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator() ){
            // FIXED: Corrected API level check and VibrationEffect usage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }

        Intent sosIntent = new Intent(dashboard.this, SosActivatedActivity.class);
        startActivity(sosIntent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Empty implementation - required by SensorEventListener interface
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-register sensor listener when activity resumes
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listener to save battery
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (sosHandler != null && sosRunnable != null) {
            sosHandler.removeCallbacks(sosRunnable);
        }
    }
}
