package com.example.doggo.Home.schedule

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.R

class ScheduleAdapter(
    private var scheduleItems: MutableList<ScheduleItem>,
    private val onDeleteClick: ((ScheduleItem, Int) -> Unit)? = null,
    private val onItemClick: ((ScheduleItem, Int) -> Unit)? = null
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconContainer: CardView = itemView.findViewById(R.id.cvIconContainer)
        val icon: ImageView = itemView.findViewById(R.id.ivScheduleIcon)
        val type: TextView = itemView.findViewById(R.id.tvScheduleType)
        val description: TextView = itemView.findViewById(R.id.tvScheduleDescription)
        val time: TextView = itemView.findViewById(R.id.tvScheduleTime)
        val duration: TextView = itemView.findViewById(R.id.tvScheduleDuration)
        val editButton: ImageButton = itemView.findViewById(R.id.btnEditSchedule)    // ← TAMBAH INI
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteSchedule)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = scheduleItems[position]

        holder.type.text = item.type.displayName
        holder.description.text = item.description
        holder.time.text = item.time
        holder.icon.setImageResource(item.type.iconRes)

        // Show duration if available
        if (!item.duration.isNullOrEmpty()) {
            holder.duration.visibility = View.VISIBLE
            holder.duration.text = " • ${item.duration}"
        } else {
            holder.duration.visibility = View.GONE
        }

        // Set icon background color based on type
        val backgroundColor = when (item.type) {
            ScheduleType.EAT -> Color.parseColor("#10B981") // Green
            ScheduleType.WALK -> Color.parseColor("#3B82F6") // Blue
            ScheduleType.SLEEP -> Color.parseColor("#8B5CF6") // Purple
            ScheduleType.MEDICINE -> Color.parseColor("#EF4444") // Red
            ScheduleType.GROOM -> Color.parseColor("#F59E0B") // Orange
        }
        holder.iconContainer.setCardBackgroundColor(backgroundColor)

        // ✅ Edit button click
        holder.editButton.setOnClickListener {
            onItemClick?.invoke(item, position)
        }

        // ✅ Delete button click
        holder.deleteButton.setOnClickListener {
            onDeleteClick?.invoke(item, position)
        }

        // ✅ Whole card click (optional - untuk user experience lebih baik)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item, position)
        }
    }

    override fun getItemCount(): Int = scheduleItems.size

    fun updateSchedule(newItems: List<ScheduleItem>) {
        scheduleItems.clear()
        scheduleItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in scheduleItems.indices) {
            scheduleItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}