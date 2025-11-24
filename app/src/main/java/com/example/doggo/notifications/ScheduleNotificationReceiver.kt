package com.example.doggo.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.doggo.Home.DogProfileDetailActivity
import com.example.doggo.R

class ScheduleNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "📬 Notification received!")

        val dogId = intent.getStringExtra("DOG_ID") ?: return
        val dogName = intent.getStringExtra("DOG_NAME") ?: "Your dog"
        val scheduleType = intent.getStringExtra("SCHEDULE_TYPE") ?: "activity"
        val scheduleTime = intent.getStringExtra("SCHEDULE_TIME") ?: ""
        val description = intent.getStringExtra("DESCRIPTION") ?: ""
        val scheduleId = intent.getStringExtra("SCHEDULE_ID") ?: ""

        showNotification(context, dogId, dogName, scheduleType, scheduleTime, description)

        // Re-schedule untuk besok (daily reminder)
        ScheduleNotificationManager.scheduleNotification(
            context = context,
            dogId = dogId,
            dogName = dogName,
            scheduleType = scheduleType,
            scheduleTime = scheduleTime,
            description = description,
            scheduleId = scheduleId
        )
    }

    private fun showNotification(
        context: Context,
        dogId: String,
        dogName: String,
        scheduleType: String,
        scheduleTime: String,
        description: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent untuk buka detail dog
        val intent = Intent(context, DogProfileDetailActivity::class.java).apply {
            putExtra("DOG_PROFILE_ID", dogId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            dogId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = ScheduleNotificationManager.getScheduleTypeEmoji(scheduleType)
        val typeName = ScheduleNotificationManager.getScheduleTypeName(scheduleType)

        // Build notification
        val title = "$emoji Reminder: $dogName's $typeName"
        val message = if (description.isNotEmpty()) {
            "At $scheduleTime - $description"
        } else {
            "Scheduled at $scheduleTime"
        }

        val notification = NotificationCompat.Builder(context, "dog_schedule_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Ganti dengan icon lo
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        // Unique notification ID untuk setiap schedule
        val notificationId = "$dogId-$scheduleType-$scheduleTime".hashCode()
        notificationManager.notify(notificationId, notification)

        Log.d("NotificationReceiver", "✅ Notification shown: $title")
    }
}