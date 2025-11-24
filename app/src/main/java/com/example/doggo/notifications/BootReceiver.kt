package com.example.doggo.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.doggo.Home.ProfileManager
import com.example.doggo.network.RetrofitClient

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "📱 Device booted, re-scheduling notifications...")

            // Re-schedule semua notifications untuk semua dogs
            val profiles = ProfileManager.getAllProfiles()

            profiles.forEach { profile ->
                val scheduleMap = mutableMapOf<String, List<com.example.doggo.network.ScheduleDetail>>()

                profile.schedule?.let { schedule ->
                    schedule.eat?.let { scheduleMap["eat"] = it }
                    schedule.walk?.let { scheduleMap["walk"] = it }
                    schedule.sleep?.let { scheduleMap["sleep"] = it }
                    schedule.medicine?.let { scheduleMap["medicine"] = it }
                    schedule.groom?.let { scheduleMap["groom"] = it }
                }

                if (scheduleMap.isNotEmpty()) {
                    ScheduleNotificationManager.rescheduleAllNotifications(
                        context = context,
                        dogId = profile.id,
                        dogName = profile.name,
                        schedules = scheduleMap
                    )
                }
            }

            Log.d("BootReceiver", "✅ Notifications re-scheduled for ${profiles.size} dogs")
        }
    }
}