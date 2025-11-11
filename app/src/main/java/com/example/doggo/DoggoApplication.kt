package com.example.doggo

import android.app.Application
import android.util.Log
import com.example.doggo.network.RetrofitClient

class DoggoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("DoggoApp", "🚀 Application onCreate called")

        // ✅ PENTING: Initialize RetrofitClient dengan context
        RetrofitClient.init(this)

        Log.d("DoggoApp", "✅ RetrofitClient initialized successfully")
    }
}