package com.example.doggo.Home.medical

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MedicalReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MedicalReminder", "📬 Medical reminder received")

        val dogName = intent.getStringExtra("DOG_NAME") ?: "Your dog"
        val medicalType = intent.getStringExtra("MEDICAL_TYPE") ?: "Medical"
        val medicalName = intent.getStringExtra("MEDICAL_NAME") ?: "Checkup"
        val medicalId = intent.getIntExtra("MEDICAL_ID", -1)

        val title = "$medicalType Due for $dogName"
        val message = "$medicalName is scheduled. Don't forget to take care of your pet!"

        MedicalNotificationManager.showNotification(
            context = context,
            medicalId = medicalId,
            dogName = dogName,
            title = title,
            message = message
        )
    }
}