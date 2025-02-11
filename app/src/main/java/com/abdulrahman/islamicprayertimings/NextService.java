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
    private int[] prayerTimingsSeconds;
    private String[] prayerTimings;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder; // Store the builder
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "MyChannel";

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prayerTimingsSeconds = intent.getIntArrayExtra("prayer_timings_seconds");
        prayerTimings = intent.getStringArrayExtra("prayer_timings");
        assert prayerTimingsSeconds != null;
        Log.d("MyService", "Received seconds: " + prayerTimingsSeconds.length);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        // Initialize the builder once
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Next pray")
                .setContentText("Time remaining: 0 seconds")
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
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


    private void updateNotification(String s, int nextIdx, String currentTimerValue) {
        // Update the existing notification
        notificationBuilder.setContentTitle(s + " " + prayerTimings[nextIdx]);
        notificationBuilder.setContentText("بعد  " + currentTimerValue);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        notificationBuilder.setWhen(0);
        notificationBuilder.setShowWhen(false);
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

        // If hours are zero, format without the hour part
        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
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
                        nextVal = prayerTimingsSeconds[i];
                    }
                    if (prayerTimingsSeconds[i] > curr) {
                        nextVal = prayerTimingsSeconds[i];
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
                    updateNotification(temp[nextIdx], nextIdx, time);
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