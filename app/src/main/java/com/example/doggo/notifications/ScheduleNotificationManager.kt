package com.example.doggo.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.doggo.Home.DogProfileDetailActivity
import com.example.doggo.R
import com.example.doggo.network.ScheduleDetail
import java.text.SimpleDateFormat
import java.util.*

object ScheduleNotificationManager {

    private const val CHANNEL_ID = "dog_schedule_channel"
    private const val CHANNEL_NAME = "Dog Schedule Reminders"
    private const val REMINDER_MINUTES_BEFORE = 30 // ⚠️ TEST MODE: 2 menit sebelum (ganti jadi 30 setelah test)

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for dog schedule reminders"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("ScheduleNotif", "✅ Notification channel created")
        }
    }

    fun scheduleNotification(
        context: Context,
        dogId: String,
        dogName: String,
        scheduleType: String,
        scheduleTime: String,
        description: String,
        scheduleId: String
    ) {
        try {
            val calendar = parseTimeToCalendar(scheduleTime)
            if (calendar == null) {
                Log.e("ScheduleNotif", "❌ Invalid time format: $scheduleTime")
                return
            }

            // Set notifikasi 30 menit sebelum waktu schedule
            calendar.add(Calendar.MINUTE, -REMINDER_MINUTES_BEFORE)

            // Kalau waktunya udah lewat hari ini, schedule untuk besok
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduleNotificationReceiver::class.java).apply {
                action = "com.example.doggo.SCHEDULE_REMINDER"
                putExtra("DOG_ID", dogId)
                putExtra("DOG_NAME", dogName)
                putExtra("SCHEDULE_TYPE", scheduleType)
                putExtra("SCHEDULE_TIME", scheduleTime)
                putExtra("DESCRIPTION", description)
                putExtra("SCHEDULE_ID", scheduleId)
            }

            // Unique request code untuk setiap schedule
            val requestCode = generateRequestCode(dogId, scheduleType, scheduleId)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Set exact alarm (perlu permission SCHEDULE_EXACT_ALARM untuk Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback ke inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Log.d("ScheduleNotif", "✅ Notification scheduled for: ${dateFormat.format(calendar.time)}")
            Log.d("ScheduleNotif", "   Dog: $dogName, Type: $scheduleType, Time: $scheduleTime")

        } catch (e: Exception) {
            Log.e("ScheduleNotif", "❌ Failed to schedule notification: ${e.message}")
        }
    }

    fun cancelNotification(context: Context, dogId: String, scheduleType: String, scheduleId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduleNotificationReceiver::class.java)
            val requestCode = generateRequestCode(dogId, scheduleType, scheduleId)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d("ScheduleNotif", "✅ Notification cancelled: $scheduleType - $scheduleId")
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "❌ Failed to cancel notification: ${e.message}")
        }
    }

    fun cancelAllNotificationsForDog(context: Context, dogId: String) {
        val scheduleTypes = listOf("eat", "walk", "sleep", "medicine", "groom")
        // Cancel untuk beberapa kemungkinan schedule IDs
        for (type in scheduleTypes) {
            for (i in 0..10) {
                cancelNotification(context, dogId, type, i.toString())
            }
        }
        Log.d("ScheduleNotif", "✅ All notifications cancelled for dog: $dogId")
    }

    fun rescheduleAllNotifications(context: Context, dogId: String, dogName: String, schedules: Map<String, List<ScheduleDetail>>) {
        // Cancel existing notifications first
        cancelAllNotificationsForDog(context, dogId)

        // Schedule new notifications
        schedules.forEach { (scheduleType, scheduleList) ->
            scheduleList.forEach { schedule ->
                if (schedule.time.isNotEmpty() && schedule.id != null) {
                    scheduleNotification(
                        context = context,
                        dogId = dogId,
                        dogName = dogName,
                        scheduleType = scheduleType,
                        scheduleTime = schedule.time,
                        description = schedule.description,
                        scheduleId = schedule.id
                    )
                }
            }
        }
    }

    private fun parseTimeToCalendar(timeString: String): Calendar? {
        return try {
            // Parse format "HH:mm" atau "HH:mm:ss"
            val parts = timeString.split(":")
            if (parts.size < 2) return null

            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null

            if (hour !in 0..23 || minute !in 0..59) return null

            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            Log.e("ScheduleNotif", "Failed to parse time: $timeString")
            null
        }
    }

    private fun generateRequestCode(dogId: String, scheduleType: String, scheduleId: String): Int {
        // Generate unique request code dari kombinasi dogId, scheduleType, dan scheduleId
        return "$dogId-$scheduleType-$scheduleId".hashCode()
    }

    fun getScheduleTypeEmoji(scheduleType: String): String {
        return when (scheduleType) {
            "eat" -> "🍽️"
            "walk" -> "🚶"
            "sleep" -> "😴"
            "medicine" -> "💊"
            "groom" -> "✂️"
            else -> "🐕"
        }
    }

    fun getScheduleTypeName(scheduleType: String): String {
        return when (scheduleType) {
            "eat" -> "Eating"
            "walk" -> "Walking"
            "sleep" -> "Sleeping"
            "medicine" -> "Medicine"
            "groom" -> "Grooming"
            else -> "Activity"
        }
    }
}