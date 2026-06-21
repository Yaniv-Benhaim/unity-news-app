package com.example.unitynewsbackend.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.unitynewsbackend.BackendRuntime
import com.example.unitynewsbackend.MainActivity
import com.example.unitynewsbackend.R

/**
 * Foreground service used to keep the backend process visible and running.
 *
 * The actual AIDL binder lives in NewsBackendService. This service is for the
 * Android lifecycle/user-visible notification requirement.
 */
class NewsBackendForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        // START_STICKY asks Android to recreate the service after process death.
        startForeground(NOTIFICATION_ID, buildNotification())
        BackendRuntime.setForegroundServiceRunning(true)
        return START_STICKY
    }

    override fun onDestroy() {
        BackendRuntime.setForegroundServiceRunning(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.backend_notification_content))
            .setOngoing(true)
            .setContentIntent(activityPendingIntent())
            .addAction(0, getString(R.string.backend_notification_action_stop), stopPendingIntent())
            .build()

    /** Tapping the notification opens the backend console. */
    private fun activityPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    /** Stop action lets the user shut down the foreground service from notification shade. */
    private fun stopPendingIntent(): PendingIntent =
        PendingIntent.getService(
            this,
            1,
            Intent(this, NewsBackendForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    /** Android 8+ requires a notification channel for foreground services. */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.backend_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "unity_news_backend"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.unitynewsbackend.action.STOP_BACKEND"

        fun intent(context: Context): Intent =
            Intent(context, NewsBackendForegroundService::class.java)
    }
}
