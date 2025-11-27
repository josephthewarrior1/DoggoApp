package com.example.doggo.Home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.R
import com.example.doggo.network.MedicalRecord
import com.google.android.material.card.MaterialCardView

class MedicalRecordAdapter(
    private var records: MutableList<MedicalRecord>,
    private val onItemClick: (MedicalRecord) -> Unit,
    private val onItemLongClick: (MedicalRecord) -> Unit
) : RecyclerView.Adapter<MedicalRecordAdapter.MedicalRecordViewHolder>() {

    class MedicalRecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardMedicalRecord)
        val tvType: TextView = itemView.findViewById(R.id.tvMedicalType)
        val tvName: TextView = itemView.findViewById(R.id.tvMedicalName)
        val tvDate: TextView = itemView.findViewById(R.id.tvMedicalDate)
        val tvNextDue: TextView = itemView.findViewById(R.id.tvNextDue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalRecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medical_record, parent, false)
        return MedicalRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicalRecordViewHolder, position: Int) {
        val record = records[position]

        holder.tvType.text = record.type.capitalize()
        holder.tvName.text = record.name
        holder.tvDate.text = "Date: ${record.date}"

        // Show next due date if available
        if (!record.nextDueDate.isNullOrEmpty()) {
            holder.tvNextDue.visibility = View.VISIBLE
            holder.tvNextDue.text = "Next Due: ${record.nextDueDate}"
        } else {
            holder.tvNextDue.visibility = View.GONE
        }

        // Click listeners
        holder.cardView.setOnClickListener {
            onItemClick(record)
        }

        holder.cardView.setOnLongClickListener {
            onItemLongClick(record)
            true
        }
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: List<MedicalRecord>) {
        records.clear()
        records.addAll(newRecords)
        notifyDataSetChanged()
    }
}