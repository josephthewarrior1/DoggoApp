package com.example.doggo.Home

data class ScheduleItem(
    val type: ScheduleType,
    val time: String,
    val description: String,
    val duration: String? = null
)

enum class ScheduleType(val displayName: String, val iconRes: Int) {
    EAT("Eat", android.R.drawable.ic_menu_today),
    WALK("Walk", android.R.drawable.ic_menu_directions),
    SLEEP("Sleep", android.R.drawable.ic_lock_idle_alarm),
    MEDICINE("Medicine", android.R.drawable.ic_menu_add),
    GROOM("Groom", android.R.drawable.ic_menu_edit)
}