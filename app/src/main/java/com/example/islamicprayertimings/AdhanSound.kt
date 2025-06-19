package com.example.islamicprayertimings

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.net.toUri

class AdhanSound : Service() {
  private var mediaPlayer: MediaPlayer? = null

  override fun onCreate() {
    super.onCreate()

    mediaPlayer = MediaPlayer()

    val audioAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_NOTIFICATION)
      .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
      .build()

    mediaPlayer?.setAudioAttributes(audioAttributes)

    val uri = "android.resource://${packageName}/${R.raw.adhan_mansour_al_zahrani}".toUri()
    mediaPlayer?.setDataSource(this, uri)

    mediaPlayer?.setOnCompletionListener {
      it.release()
      stopSelf()
    }

    mediaPlayer?.prepareAsync()

    mediaPlayer?.setOnPreparedListener {
      it.start()
    }
  }

  override fun onDestroy() {
    mediaPlayer?.release()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
