package com.example.doggo.Home.schedule

import android.R

data class ScheduleItem(
    val id: String? = null,
    val type: ScheduleType,
    val time: String,
    val description: String,
    val duration: String? = null
)

enum class ScheduleType(val displayName: String, val iconRes: Int, val apiName: String) {
    EAT("Eat", R.drawable.ic_menu_today, "eat"),           // ← TAMBAH "eat"
    WALK("Walk", R.drawable.ic_menu_directions, "walk"),   // ← TAMBAH "walk"
    SLEEP("Sleep", R.drawable.ic_lock_idle_alarm, "sleep"), // ← TAMBAH "sleep"
    MEDICINE("Medicine", R.drawable.ic_menu_add, "medicine"), // ← TAMBAH "medicine"
    GROOM("Groom", R.drawable.ic_menu_edit, "groom")       // ← TAMBAH "groom"
}