package com.example.doggo.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.databinding.ItemDogProfileCardBinding

class DogProfileAdapter(
    private var dogProfiles: MutableList<DogProfile>,
    private val onItemClick: (DogProfile) -> Unit
) : RecyclerView.Adapter<DogProfileAdapter.DogProfileViewHolder>() {

    inner class DogProfileViewHolder(private val binding: ItemDogProfileCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dogProfile: DogProfile) {
            binding.tvDogName.text = dogProfile.name
            binding.tvDogInfo.text = "${dogProfile.gender} | ${dogProfile.breed}"

            // TODO: Load photo from URL when implementing image storage
            // For now, it will use the placeholder from layout

            binding.root.setOnClickListener {
                onItemClick(dogProfile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogProfileViewHolder {
        val binding = ItemDogProfileCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DogProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DogProfileViewHolder, position: Int) {
        holder.bind(dogProfiles[position])
    }

    override fun getItemCount(): Int = dogProfiles.size

    fun updateProfiles(newProfiles: List<DogProfile>) {
        dogProfiles.clear()
        dogProfiles.addAll(newProfiles)
        notifyDataSetChanged()
    }

    fun addProfile(profile: DogProfile) {
        dogProfiles.add(profile)
        notifyItemInserted(dogProfiles.size - 1)
    }
}