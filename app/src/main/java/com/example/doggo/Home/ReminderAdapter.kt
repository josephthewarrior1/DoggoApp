package com.example.doggo.Home

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        holder.tvTitle.text = reminder.title
        holder.tvDescription.text = reminder.description

        // Format due date display
        val dueText = if (reminder.minutesUntil != null) {
            when {
                reminder.minutesUntil < 0 -> "Overdue by ${-reminder.minutesUntil} mins"
                reminder.minutesUntil <= 30 -> "In ${reminder.minutesUntil} mins"
                else -> "In ${reminder.minutesUntil / 60} hours"
            }
        } else {
            "Due: ${reminder.dueDate}"
        }
        holder.tvDueDate.text = dueText

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