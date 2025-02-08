package com.abdulrahman.islamicprayertimings;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;


public class NextService extends Service {
    private int[] prayerTimings;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder; // Store the builder
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MyChannel";

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prayerTimings = intent.getIntArrayExtra("prayer_timings");
        assert prayerTimings != null;
        Log.d("MyService", "Received seconds: " + prayerTimings.length);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        // Initialize the builder once
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Next pray")
                .setContentText("Time remaining: 0 seconds")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOnlyAlertOnce(true); // Prevents repeated sounds
        startForeground(NOTIFICATION_ID, notificationBuilder.build());

        setNext();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Prayer Timings Channel", NotificationManager.IMPORTANCE_LOW); // Or IMPORTANCE_DEFAULT
            channel.setDescription("Channel for Prayer Time Notifications");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void updateNotification(String s, String currentTimerValue) {
        // Update the existing notification
        notificationBuilder.setContentTitle(s);
        notificationBuilder.setContentText("بعد  " + currentTimerValue);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @SuppressLint("DefaultLocale")
    private String secondsToHHMMSS(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    private void setNext() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                int seconds = calendar.get(Calendar.SECOND);
                int curr = (hours * 3600) + (minutes * 60) + seconds;

                int nextIdx = 0, nextVal = 0;
                String[] temp = {"صلاة الفجر", "الشروق", "صلاة الظهر", "صلاة العصر", "صلاة المغرب", "صلاة العشاء"};
                for (int i = 0; i < 6; i++) {
                    if (i == 0) {
                        nextVal = prayerTimings[i];
                    }
                    if (prayerTimings[i] > curr) {
                        nextVal = prayerTimings[i];
                        nextIdx = i;
                        break;
                    }
                }
                int dff = nextVal - curr;
                if (dff < 0) dff += 24 * 60 * 60;
                Log.d("MyService", "Next pray: " + nextIdx + ", " + nextVal);

                if (dff > 0) {
                    dff--;
                    String time = secondsToHHMMSS(dff);
                    updateNotification(temp[nextIdx], time);
                    handler.postDelayed(this, 1000);
                } else {
                    setNext();
                    stopSelf();
                }
            }
        };
        handler.post(runnable);
    }
}