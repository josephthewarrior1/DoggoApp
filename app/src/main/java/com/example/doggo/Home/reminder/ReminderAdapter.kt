package com.example.doggo.Home.reminder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.R
import com.google.android.material.card.MaterialCardView
import kotlin.math.abs

class ReminderAdapter(
    private val onItemClick: (ReminderItem) -> Unit,
    private val onDeleteClick: ((ReminderItem) -> Unit)? = null
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private val reminders = mutableListOf<ReminderItem>()

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardReminder)
        val tvIcon: TextView = itemView.findViewById(R.id.tvReminderIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvReminderTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvReminderDescription)
        val tvTime: TextView = itemView.findViewById(R.id.tvReminderTime)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        // Text
        holder.tvTitle.text = reminder.title
        holder.tvDescription.text = reminder.description

        // Time label (right side)
        holder.tvTime.text = buildTimeLabel(reminder.minutesUntil, reminder.dueDate)

        // Icon
        holder.tvIcon.text = getIconForReminder(reminder)

        // Time color by status
        holder.tvTime.setTextColor(
            when (reminder.status) {
                ReminderStatus.OVERDUE   -> Color.parseColor("#FF3B30") // red
                ReminderStatus.UPCOMING  -> Color.parseColor("#FF9500") // orange
                ReminderStatus.COMPLETED -> Color.parseColor("#34C759") // green
                ReminderStatus.PENDING   -> Color.parseColor("#8A8A8A") // grey
            }
        )

        // Clicks
        holder.cardView.setOnClickListener { onItemClick(reminder) }
        holder.btnDelete.setOnClickListener { onDeleteClick?.invoke(reminder) }
    }

    override fun getItemCount(): Int = reminders.size

    fun updateReminders(newReminders: List<ReminderItem>) {
        reminders.clear()
        reminders.addAll(newReminders)
        notifyDataSetChanged()
    }

    // --- Helpers ---

    // For now, iOS-style: only show time (HH:mm or date string)
    private fun buildTimeLabel(minutesUntil: Int?, time: String): String {
        // You can later change to relative text if you want.
        return time
    }

    // Icon mapping:
    //  MEDICAL  -> 💊
    //  SCHEDULE:
    //    walking → 🚶
    //    sleep   → 🌙
    //    eating  → 🍽️
    //    other   → 🔔
    private fun getIconForReminder(reminder: ReminderItem): String {
        return when (reminder.type) {
            ReminderType.MEDICAL -> "💊"
            ReminderType.SCHEDULE -> {
                val text = (reminder.title + " " + reminder.description).lowercase()

                when {
                    "walk" in text || "walking" in text -> "🚶"
                    "sleep" in text || "bed" in text || "nap" in text -> "🌙"
                    "eat" in text || "feed" in text ||
                            "lunch" in text || "dinner" in text || "breakfast" in text -> "🍽️"
                    else -> "🔔"
                }
            }
        }
    }
}