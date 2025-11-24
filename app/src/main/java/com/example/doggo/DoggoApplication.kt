package com.example.doggo

import android.app.Application
import android.util.Log
import com.example.doggo.network.RetrofitClient
import com.example.doggo.notifications.ScheduleNotificationManager

class DoggoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Retrofit
        RetrofitClient.init(this)
        Log.d("DoggoApplication", "✅ RetrofitClient initialized")

        // Create Notification Channel
        ScheduleNotificationManager.createNotificationChannel(this)
        Log.d("DoggoApplication", "✅ Notification channel created")

        Log.d("DoggoApplication", "✅ Application initialized")
    }
}