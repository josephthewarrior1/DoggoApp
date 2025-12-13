package com.example.doggo.Home.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.R
import com.example.doggo.network.ScheduleDetail
import com.google.android.material.card.MaterialCardView

class ScheduleAdapter(
    private val onItemClick: (ScheduleDetail) -> Unit,
    private val onDeleteClick: (ScheduleDetail) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    private val schedules = mutableListOf<ScheduleDetail>()

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardSchedule)
        val tvTime: TextView = itemView.findViewById(R.id.tvScheduleTime)
        val tvDescription: TextView = itemView.findViewById(R.id.tvScheduleDescription)
        val tvDuration: TextView = itemView.findViewById(R.id.tvScheduleDuration)
        val btnDelete: TextView = itemView.findViewById(R.id.btnDeleteSchedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]

        holder.tvTime.text = schedule.time
        holder.tvDescription.text = schedule.description

        // Show duration if available
        if (!schedule.duration.isNullOrEmpty()) {
            holder.tvDuration.visibility = View.VISIBLE
            holder.tvDuration.text = "Duration: ${schedule.duration}"
        } else {
            holder.tvDuration.visibility = View.GONE
        }

        // Click listeners
        holder.cardView.setOnClickListener {
            onItemClick(schedule)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(schedule)
        }
    }

    override fun getItemCount(): Int = schedules.size

    fun updateSchedules(newSchedules: List<ScheduleDetail>) {
        schedules.clear()
        schedules.addAll(newSchedules)
        notifyDataSetChanged()
    }
}