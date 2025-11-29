package com.example.doggo.Home.schedule

import android.R

data class ScheduleItem(
    val type: ScheduleType,
    val time: String,
    val description: String,
    val duration: String? = null
)

enum class ScheduleType(val displayName: String, val iconRes: Int) {
    EAT("Eat", R.drawable.ic_menu_today),
    WALK("Walk", R.drawable.ic_menu_directions),
    SLEEP("Sleep", R.drawable.ic_lock_idle_alarm),
    MEDICINE("Medicine", R.drawable.ic_menu_add),
    GROOM("Groom", R.drawable.ic_menu_edit)
}