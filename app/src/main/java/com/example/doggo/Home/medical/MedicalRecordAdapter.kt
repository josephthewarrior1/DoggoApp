package com.example.doggo.Home.medical

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.R
import com.example.doggo.network.MedicalRecord
import com.google.android.material.card.MaterialCardView

class MedicalRecordAdapter(
    private val onItemClick: (MedicalRecord) -> Unit,
    private val onItemLongClick: (MedicalRecord) -> Unit
) : RecyclerView.Adapter<MedicalRecordAdapter.MedicalRecordViewHolder>() {

    private val records = mutableListOf<MedicalRecord>()

    class MedicalRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardMedicalRecord)
        val tvName: TextView = itemView.findViewById(R.id.tvMedicalName)
        val tvDate: TextView = itemView.findViewById(R.id.tvMedicalDate)
        val tvNextDue: TextView = itemView.findViewById(R.id.tvNextDue)
        val tvClinic: TextView = itemView.findViewById(R.id.tvMedicalClinic)
        val tvStatus: TextView = itemView.findViewById(R.id.tvMedicalStatus)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteMedical)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_record, parent, false)
        return MedicalRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicalRecordViewHolder, position: Int) {
        val record = records[position]

        holder.tvName.text = record.name
        holder.tvDate.text = "📅 ${record.date}"

        // Show next due date if available
        if (!record.nextDueDate.isNullOrEmpty()) {
            holder.tvNextDue.visibility = View.VISIBLE
            holder.tvNextDue.text = "⏰ Next: ${record.nextDueDate}"
        } else {
            holder.tvNextDue.visibility = View.GONE
        }

        // Show clinic if available
        if (!record.clinic.isNullOrEmpty()) {
            holder.tvClinic.visibility = View.VISIBLE
            holder.tvClinic.text = "🏥 ${record.clinic}"
        } else {
            holder.tvClinic.visibility = View.GONE
        }

        // Status badge
        holder.tvStatus.text = record.status?.capitalize() ?: "Completed"

        // Click listeners
        holder.cardView.setOnClickListener {
            onItemClick(record)
        }

        holder.btnDelete.setOnClickListener {
            onItemLongClick(record)
        }
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: List<MedicalRecord>) {
        records.clear()
        records.addAll(newRecords)
        notifyDataSetChanged()
    }
}