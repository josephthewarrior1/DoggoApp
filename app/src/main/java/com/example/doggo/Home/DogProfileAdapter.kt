package com.example.doggo.Home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.R

class DogProfileAdapter(
    private var profiles: MutableList<DogProfile>,
    private val onItemClick: (DogProfile) -> Unit
) : RecyclerView.Adapter<DogProfileAdapter.DogProfileViewHolder>() {

    class DogProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ✅ SESUAIKAN DENGAN LAYOUT YANG ADA
        val dogImage: ImageView = itemView.findViewById(R.id.ivDogPhotoImage)  // dari ivDogPhotoImage
        val dogName: TextView = itemView.findViewById(R.id.tvDogName)          // sama
        val dogBreed: TextView = itemView.findViewById(R.id.tvDogBreed)        // sama
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dog_profile_card, parent, false)
        return DogProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogProfileViewHolder, position: Int) {
        val profile = profiles[position]

        holder.dogName.text = profile.name
        holder.dogBreed.text = profile.breed

        // TODO: Load image menggunakan Glide/Picasso nanti
        // Untuk sekarang, gunakan placeholder

        holder.itemView.setOnClickListener {
            onItemClick(profile)
        }
    }

    override fun getItemCount(): Int = profiles.size

    fun updateProfiles(newProfiles: List<DogProfile>) {
        profiles.clear()
        profiles.addAll(newProfiles)
        notifyDataSetChanged()
    }
}