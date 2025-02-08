package com.abdulrahman.islamicprayertimings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private String country = "Egypt", city = "Cairo";
    private EditText Country, City;
    private Button update;
    private static final String aladhanAPI = "https://api.aladhan.com/v1/timingsByCity?";
    private ExecutorService executorService; // Executor for background tasks
    private Handler mainHandler; // Handler to update the UI thread
    private TextView fajr, shrook, duhr, asr, maghrib, isha;
    public int[] prayerTimings;

    // SharedPreferences name and keys
    private static final String PREFS_NAME = "PrayerAppPrefs";
    private static final String KEY_COUNTRY = "country";
    private static final String KEY_CITY = "city";
    private static final String KEY_FAJR = "fajr";
    private static final String KEY_SHROOK = "shrook";
    private static final String KEY_DUHR = "duhr";
    private static final String KEY_ASR = "asr";
    private static final String KEY_MAGHRIB = "maghrib";
    private static final String KEY_ISHA = "isha";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        initUI();
        // Initialize ExecutorService and Handler
        executorService = Executors.newSingleThreadExecutor(); // For single background task
        mainHandler = new Handler(Looper.getMainLooper()); // For updating UI from the background thread

        // Get permission for notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the app has the POST_NOTIFICATIONS permission
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }

        // Load saved country and city from SharedPreferences
        loadPreferences();
        setupListeners();
        getTimings();
    }

    private void initUI() {
        fajr = findViewById(R.id.fajr);
        shrook = findViewById(R.id.shrook);
        duhr = findViewById(R.id.duhr);
        asr = findViewById(R.id.asr);
        maghrib = findViewById(R.id.maghrib);
        isha = findViewById(R.id.isha);
        Country = findViewById(R.id.Country);
        City = findViewById(R.id.City);
        update = findViewById(R.id.button);
    }

    private void setupListeners() {
        update.setOnClickListener(view -> {
            // Save the entered values when the update button is clicked
            savePreferences();
            getTimings();
        });
    }

    private void savePreferences() {
        // Get the values entered by the user
        country = Country.getText().toString().trim();
        city = City.getText().toString().trim();

        // Store the values in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_COUNTRY, country);
        editor.putString(KEY_CITY, city);
        editor.apply();
    }

    private void loadPreferences() {
        // Retrieve the stored values from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        country = sharedPreferences.getString(KEY_COUNTRY, "Egypt");
        city = sharedPreferences.getString(KEY_CITY, "Cairo");
        Country.setText(country);
        City.setText(city);

        // Load prayer timings
        fajr.setText(sharedPreferences.getString(KEY_FAJR, ""));
        shrook.setText(sharedPreferences.getString(KEY_SHROOK, ""));
        duhr.setText(sharedPreferences.getString(KEY_DUHR, ""));
        asr.setText(sharedPreferences.getString(KEY_ASR, ""));
        maghrib.setText(sharedPreferences.getString(KEY_MAGHRIB, ""));
        isha.setText(sharedPreferences.getString(KEY_ISHA, ""));
    }

    private void savePrayerTimings(String fajrTime, String shrookTime, String duhrTime,
                                   String asrTime, String maghribTime, String ishaTime) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FAJR, fajrTime);
        editor.putString(KEY_SHROOK, shrookTime);
        editor.putString(KEY_DUHR, duhrTime);
        editor.putString(KEY_ASR, asrTime);
        editor.putString(KEY_MAGHRIB, maghribTime);
        editor.putString(KEY_ISHA, ishaTime);
        editor.apply();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void getTimings() {
        executorService.execute(() -> {
            try {
                // Use the loaded country and city
                URL url = new URL(aladhanAPI + "city=" + city + "&country=" + country);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    showToast(conn.getResponseMessage());
                    return;
                }
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                // Parse the response JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject data = jsonResponse.getJSONObject("data");
                JSONObject timings = data.getJSONObject("timings");
                String Fajr = timings.getString("Fajr");
                String Shrook = timings.getString("Sunrise");
                String Duhr = timings.getString("Dhuhr");
                String Asr = timings.getString("Asr");
                String Maghrib = timings.getString("Maghrib");
                String Isha = timings.getString("Isha");

                // Save prayer timings to SharedPreferences
                savePrayerTimings(Fajr, Shrook, Duhr, Asr, Maghrib, Isha);

                mainHandler.post(() -> {
                    String[] temp = {Fajr, Shrook, Duhr, Asr, Maghrib, Isha};
                    prayerTimings = new int[6];
                    for (int i = 0; i < 6; i++) {
                        String[] timeParts = temp[i].split(":");
                        int hours = Integer.parseInt(timeParts[0]);
                        int minutes = Integer.parseInt(timeParts[1]);
                        int totalSeconds = (hours * 3600) + (minutes * 60);
                        prayerTimings[i] = totalSeconds;
                    }

                    fajr.setText(Fajr);
                    shrook.setText(Shrook);
                    duhr.setText(Duhr);
                    asr.setText(Asr);
                    maghrib.setText(Maghrib);
                    isha.setText(Isha);

                    Intent serviceIntent = new Intent(this, NextService.class);
                    serviceIntent.putExtra("prayer_timings", prayerTimings);
                    startService(serviceIntent);
                });
            } catch (IOException e) {
                showToast("Error getting timings from API");
            } catch (JSONException e) {
                showToast("Error parsing JSON response");
            }
        });
    }
}
