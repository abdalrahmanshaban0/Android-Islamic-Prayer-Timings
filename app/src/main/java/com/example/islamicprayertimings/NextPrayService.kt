package com.example.islamicprayertimings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.util.Calendar
import java.util.Locale

class NextPrayService : Service() {
  private val channelID = "prayer_channel"
  private val notificationID = 1
  private lateinit var notificationManager : NotificationManager
  private lateinit var notificationBuilder : NotificationCompat.Builder
  private lateinit var inboxStyle : NotificationCompat.InboxStyle
  private var prayerTimings : Array<String> = Array<String>(6) {""}
  private var prayerTimingsInSeconds : IntArray = IntArray(6)
  private var adhanSound : Boolean? = false

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }
  override fun onCreate() {
    super.onCreate()
    // Create notification channel
    notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        channelID,
        "Prayer Notification Channel",
        NotificationManager.IMPORTANCE_LOW
      )
      notificationManager.createNotificationChannel(channel)
    }

    // Open the app when the user taps the notification
    val intentOpenApp = Intent(this, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
      this,
      0,
      intentOpenApp,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    notificationBuilder = NotificationCompat.Builder(this, channelID)
      .setOngoing(true)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setOnlyAlertOnce(true)
      .setContentIntent(pendingIntent)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setWhen(0)
      .setShowWhen(false)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    startForeground(notificationID, notificationBuilder.build())

    prayerTimings = intent?.getStringArrayExtra("PrayerTimings") ?: Array(6) { "--:--" }
    prayerTimingsInSeconds = intent?.getIntArrayExtra("prayerTimingsInSeconds") ?: IntArray(6)
    adhanSound = intent?.getBooleanExtra("adhanSound", false) == true

    // Notification inbox
    inboxStyle = NotificationCompat.InboxStyle()
    for (i in 0..5) {
      inboxStyle.addLine("${getString(getPrayerNameResId(i))} : ${prayerTimings[i]}")
    }
    notificationBuilder.setStyle(inboxStyle)

    setNext()
    return START_STICKY
  }

  private fun updateNotification(s: String?, nextIdx: Int, currentTimerValue: String?) {
    notificationBuilder.setContentTitle(s + " " + prayerTimings[nextIdx])
    notificationBuilder.setContentText(getString(R.string.after) + "  " + currentTimerValue)
    notificationManager.notify(notificationID, notificationBuilder.build())
  }

  private fun setNext() {
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
      override fun run() {
        val calendar = Calendar.getInstance()
        val currSeconds = calendar.get(Calendar.HOUR_OF_DAY) * 3600 +
                calendar.get(Calendar.MINUTE) * 60 +
                calendar.get(Calendar.SECOND)

        // Determine the next pray
        var nextIdx = -1
        for (i in prayerTimingsInSeconds.indices) {
          if (prayerTimingsInSeconds[i] > currSeconds) {
            nextIdx = i
            break
          }
        }

        // If no future prayer today, set next as tomorrow's Fajr
        if (nextIdx == -1) nextIdx = 0
        var nextVal = prayerTimingsInSeconds[nextIdx]

        // Adjust for next day if needed
        var diff = nextVal - currSeconds
        if (diff < 0) diff += 24 * 3600

        if (diff <= 1 && adhanSound == true && nextIdx != 1) {
          val intent = Intent(this@NextPrayService, AdhanSound::class.java)
          startService(intent)
        }

        // Countdown and update notification
        val countdown = secondsToHHMMSS(diff)
        val prayName = getString(getPrayerNameResId(nextIdx))
        updateNotification(prayName, nextIdx, countdown)
        handler.postDelayed(this, 1000)
      }
    }
    handler.post(runnable)
  }

  private fun getPrayerNameResId(index: Int): Int {
    return when (index) {
      0 -> R.string.Fajr
      1 -> R.string.Sunrise
      2 -> R.string.Dhuhr
      3 -> R.string.Asr
      4 -> R.string.Maghrib
      5 -> R.string.Isha
      else -> R.string.app_name
    }
  }

  private fun secondsToHHMMSS(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours == 0) {
      String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    } else {
      String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
  }
}