package com.example.doggo.Home.medical

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.doggo.Home.HomeActivity
import com.example.doggo.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object MedicalNotificationManager {

    private const val CHANNEL_ID = "medical_reminders"
    private const val CHANNEL_NAME = "Medical Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for medical checkups, vaccinations, etc."
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)

            Log.d("MedicalNotification", "✅ Notification channel created")
        }
    }

    fun scheduleMedicalReminder(
        context: Context,
        medicalId: Int,
        dogId: Int,
        dogName: String,
        medicalType: String,
        medicalName: String,
        dueDate: String,
        reminderDaysBefore: Int = 7
    ) {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dueDateTime = dateFormat.parse(dueDate) ?: return

            // Calculate reminder date (X days before due date)
            val reminderCalendar = Calendar.getInstance().apply {
                time = dueDateTime
                add(Calendar.DAY_OF_YEAR, -reminderDaysBefore)
                set(Calendar.HOUR_OF_DAY, 9) // 9 AM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // Don't schedule if reminder date is in the past
            if (reminderCalendar.timeInMillis < System.currentTimeMillis()) {
                Log.d("MedicalNotification", "⏰ Reminder date is in the past, skipping")
                return
            }

            val intent = Intent(context, MedicalReminderReceiver::class.java).apply {
                action = "com.example.doggo.MEDICAL_REMINDER"  // ✅ FIX: action = bukan setAction =
                putExtra("MEDICAL_ID", medicalId)
                putExtra("DOG_ID", dogId)
                putExtra("DOG_NAME", dogName)
                putExtra("MEDICAL_TYPE", medicalType)
                putExtra("MEDICAL_NAME", medicalName)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medicalId, // Use medicalId as request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderCalendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d("MedicalNotification", "✅ Medical reminder scheduled for $dogName at ${reminderCalendar.time}")
                } else {
                    Log.e("MedicalNotification", "❌ Cannot schedule exact alarms")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderCalendar.timeInMillis,
                    pendingIntent
                )
                Log.d("MedicalNotification", "✅ Medical reminder scheduled for $dogName")
            }

        } catch (e: Exception) {
            Log.e("MedicalNotification", "❌ Failed to schedule reminder: ${e.message}")
        }
    }

    fun cancelMedicalReminder(context: Context, medicalId: Int) {
        try {
            val intent = Intent(context, MedicalReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medicalId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d("MedicalNotification", "✅ Medical reminder cancelled: $medicalId")
        } catch (e: Exception) {
            Log.e("MedicalNotification", "❌ Failed to cancel reminder: ${e.message}")
        }
    }

    fun showNotification(
        context: Context,
        medicalId: Int,
        dogName: String,
        title: String,
        message: String
    ) {
        // ✅ Intent ke HomeActivity dengan extras untuk buka tab Reminders
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // ✅ FIX: flags = bukan setFlags =
            putExtra("OPEN_REMINDERS_TAB", true)  // Extra flag untuk buka tab Reminders
            putExtra("MEDICAL_ID", medicalId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            medicalId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medical)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(medicalId, notification)

        Log.d("MedicalNotification", "✅ Notification shown: $title")
    }
}