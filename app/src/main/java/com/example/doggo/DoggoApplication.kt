package com.example.doggo

import android.app.Application
import android.util.Log
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.DogsResponse
import com.example.doggo.network.ScheduleDetail
import com.example.doggo.notifications.ScheduleNotificationManager
import com.example.doggo.Home.medical.MedicalNotificationManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DoggoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("DoggoApp", "🚀 Application starting...")

        // Initialize RetrofitClient with context
        RetrofitClient.init(this)

        // Create notification channels
        ScheduleNotificationManager.createNotificationChannel(this)
        MedicalNotificationManager.createNotificationChannel(this)

        // ✅ TAMBAH: Reschedule all notifications saat app start
        rescheduleAllNotificationsForAllDogs()

        Log.d("DoggoApp", "✅ Application initialized")
    }

    private fun rescheduleAllNotificationsForAllDogs() {
        Log.d("DoggoApp", "📅 Rescheduling notifications for all dogs...")

        // Load all dogs from API
        RetrofitClient.instance.getMyDogs().enqueue(object : Callback<DogsResponse> {
            override fun onResponse(
                call: Call<DogsResponse>,
                response: Response<DogsResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val dogs = response.body()?.dogs?.values?.toList() ?: emptyList()
                    Log.d("DoggoApp", "📊 Found ${dogs.size} dogs to schedule")

                    dogs.forEach { dog ->
                        val schedule = dog.schedule
                        if (schedule == null) {
                            Log.d("DoggoApp", "⏭️ ${dog.name} has no schedule, skipping")
                            return@forEach
                        }

                        val scheduleMap = mutableMapOf<String, List<ScheduleDetail>>()

                        schedule.eat?.let {
                            scheduleMap["eat"] = it
                            Log.d("DoggoApp", "   🍽️ ${it.size} eating schedules")
                        }
                        schedule.walk?.let {
                            scheduleMap["walk"] = it
                            Log.d("DoggoApp", "   🚶 ${it.size} walking schedules")
                        }
                        schedule.sleep?.let {
                            scheduleMap["sleep"] = it
                            Log.d("DoggoApp", "   😴 ${it.size} sleeping schedules")
                        }
                        schedule.medicine?.let {
                            scheduleMap["medicine"] = it
                            Log.d("DoggoApp", "   💊 ${it.size} medicine schedules")
                        }
                        schedule.groom?.let {
                            scheduleMap["groom"] = it
                            Log.d("DoggoApp", "   ✂️ ${it.size} grooming schedules")
                        }

                        if (scheduleMap.isNotEmpty()) {
                            ScheduleNotificationManager.rescheduleAllNotifications(
                                context = this@DoggoApplication,
                                dogId = dog.dogId.toString(),
                                dogName = dog.name,
                                schedules = scheduleMap
                            )
                            Log.d("DoggoApp", "✅ Notifications scheduled for ${dog.name}")
                        } else {
                            Log.d("DoggoApp", "⏭️ ${dog.name} has empty schedules, skipping")
                        }
                    }

                    Log.d("DoggoApp", "✅ All notifications rescheduled successfully")
                } else {
                    Log.e("DoggoApp", "❌ Failed to load dogs: ${response.body()?.error}")
                }
            }

            override fun onFailure(call: Call<DogsResponse>, t: Throwable) {
                Log.e("DoggoApp", "❌ Network error loading dogs: ${t.message}")
            }
        })
    }
}