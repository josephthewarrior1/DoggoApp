package com.example.doggo

import android.app.Application
import com.example.doggo.network.RetrofitClient
import android.util.Log

class DoggoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize RetrofitClient dengan context
        RetrofitClient.init(this)
        Log.d("DoggoApplication", "✅ Application initialized")
    }
}