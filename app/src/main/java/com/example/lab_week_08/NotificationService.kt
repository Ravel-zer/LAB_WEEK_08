package com.example.lab_week_08

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // Thread khusus background countdown
        val handlerThread = HandlerThread("CountdownThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)

        // Buat channel dan builder awal
        val channelId = createNotificationChannel()
        val pendingIntent = getPendingIntent()

        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Second worker process is done")
            .setContentText("10 seconds until last warning")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        // Tampilkan notifikasi pertama kali
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(intent, flags, startId)

        val Id = intent?.getStringExtra(EXTRA_ID) ?: "001"

        // Jalankan countdown di thread background
        serviceHandler.post {
            startCountdown(notificationBuilder)
            notifyCompletion(Id)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return returnValue
    }

    private fun startCountdown(builder: NotificationCompat.Builder) {
        val notificationManager =
            ContextCompat.getSystemService(this, NotificationManager::class.java)

        for (i in 9 downTo 0) {
            builder.setContentText("$i seconds until last warning")
            builder.setSilent(true)
            notificationManager?.notify(NOTIFICATION_ID, builder.build())
            Thread.sleep(1000L)
        }
    }

    private fun createNotificationChannel(): String {
        val channelId = "lab_week_08_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "LAB_WEEK_08",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Background countdown updates"
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
        const val NOTIFICATION_ID = 1001
        const val EXTRA_ID = "Id"

        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
