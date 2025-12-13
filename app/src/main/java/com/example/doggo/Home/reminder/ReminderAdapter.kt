package com.example.doggo.Home.reminder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.R
import com.google.android.material.card.MaterialCardView

class ReminderAdapter(
    private val onItemClick: (ReminderItem) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private val reminders = mutableListOf<ReminderItem>()

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardReminder)
        val tvTitle: TextView = itemView.findViewById(R.id.tvReminderTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvReminderDescription)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvReminderDueDate)
        val tvStatus: TextView = itemView.findViewById(R.id.tvReminderStatus)
    }

    private fun formatDueText(minutesUntil: Int?, time: String): String {
        if (minutesUntil == null) return time

        val abs = kotlin.math.abs(minutesUntil)
        val hours = abs / 60
        val mins = abs % 60

        val relative = when {
            minutesUntil < 0 && hours > 0 ->
                "Overdue by $hours h"
            minutesUntil < 0 ->
                "Overdue by $mins min"
            hours > 0 && mins > 0 ->
                "In $hours h $mins min"
            hours > 0 ->
                "In $hours hour${if (hours > 1) "s" else ""}"
            else ->
                "In $mins min"
        }

        return "$relative · $time"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        holder.tvTitle.text = reminder.title
        holder.tvDescription.text = reminder.description

        holder.tvDueDate.text = formatDueText(
            reminder.minutesUntil,
            reminder.dueDate
        )

        // Set status text and color
        holder.tvStatus.text = reminder.status.name
        val statusColor = when (reminder.status) {
            ReminderStatus.OVERDUE -> Color.RED
            ReminderStatus.UPCOMING -> Color.parseColor("#FF9800")
            ReminderStatus.COMPLETED -> Color.GREEN
            else -> Color.GRAY
        }
        holder.tvStatus.setTextColor(statusColor)

        holder.cardView.setOnClickListener {
            onItemClick(reminder)
        }
    }

    override fun getItemCount(): Int = reminders.size

    fun updateReminders(newReminders: List<ReminderItem>) {
        reminders.clear()
        reminders.addAll(newReminders)
        notifyDataSetChanged()
    }
}