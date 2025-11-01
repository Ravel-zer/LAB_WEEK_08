package com.example.lab_week_08

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SecondNotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        val handlerThread = HandlerThread("SecondCountdownThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)

        val channelId = createNotificationChannel()
        val pendingIntent = getPendingIntent()

        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Third worker process is done")
            .setContentText("10 seconds until mission complete")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val Id = intent?.getStringExtra(EXTRA_ID) ?: "002"

        serviceHandler.post {
            startCountdown(notificationBuilder)
            notifyCompletion(Id)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startCountdown(builder: NotificationCompat.Builder) {
        val notificationManager =
            ContextCompat.getSystemService(this, NotificationManager::class.java)

        for (i in 9 downTo 0) {
            builder.setContentText("$i seconds until mission complete")
            builder.setSilent(true)
            notificationManager?.notify(NOTIFICATION_ID, builder.build())
            Thread.sleep(1000L)
        }
    }

    private fun createNotificationChannel(): String {
        val channelId = "lab_week_08_channel_2"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "LAB_WEEK_08_2",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Second notification channel"
            }
            val manager = ContextCompat.getSystemService(this, NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
        return channelId
    }

    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else 0

        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            flag
        )
    }

    private fun notifyCompletion(Id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = Id
        }
    }

    companion object {
        const val NOTIFICATION_ID = 2002
        const val EXTRA_ID = "Id2"

        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
