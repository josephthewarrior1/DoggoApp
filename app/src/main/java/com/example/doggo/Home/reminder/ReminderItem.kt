package com.example.doggo.Home.reminder

import com.example.doggo.network.MedicalRecord
import com.example.doggo.network.ScheduleDetail

data class ReminderItem(
    val id: String,
    val dogId: Int,
    val dogName: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val type: ReminderType,
    val status: ReminderStatus,
    val medicalRecord: MedicalRecord? = null,
    val scheduleDetail: ScheduleDetail? = null,
    val minutesUntil: Int? = null
)

enum class ReminderType {
    MEDICAL,
    SCHEDULE
}

enum class ReminderStatus {
    PENDING,
    UPCOMING,
    OVERDUE,
    COMPLETED
}