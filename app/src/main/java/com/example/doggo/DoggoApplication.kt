package com.example.doggo

import android.app.Application
import android.util.Log
import com.example.doggo.network.RetrofitClient
import com.example.doggo.notifications.ScheduleNotificationManager
import com.example.doggo.Home.medical.MedicalNotificationManager

class DoggoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("DoggoApp", "🚀 Application starting...")

        // Initialize RetrofitClient with context
        RetrofitClient.init(this)

        // Create notification channels
        ScheduleNotificationManager.createNotificationChannel(this)
        MedicalNotificationManager.createNotificationChannel(this)

        Log.d("DoggoApp", "✅ Application initialized")
    }
}