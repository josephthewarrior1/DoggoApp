package com.example.doggo.Home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doggo.databinding.ItemDogProfileCardBinding

class DogProfileAdapter(
    private val profiles: MutableList<DogProfile>,
    private val onClick: (DogProfile) -> Unit
) : RecyclerView.Adapter<DogProfileAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDogProfileCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: DogProfile) {
            binding.tvDogName.text = profile.name
            binding.tvDogBreed.text = "Dog | ${profile.breed}"

            // TODO: Load photo from URL when implementing image storage
            // For now, it will use the placeholder from layout

            binding.root.setOnClickListener {
                onClick(profile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDogProfileCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = profiles.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    /**
     * Replace adapter data and refresh the RecyclerView.
     */
    fun updateProfiles(newProfiles: List<DogProfile>) {
        android.util.Log.d("DogProfileAdapter", "updateProfiles called with ${newProfiles.size} profiles")
        profiles.clear()
        android.util.Log.d("DogProfileAdapter", "profiles cleared, size: ${profiles.size}")
        profiles.addAll(newProfiles)
        android.util.Log.d("DogProfileAdapter", "profiles after addAll: ${profiles.size}")
        notifyDataSetChanged()
        android.util.Log.d("DogProfileAdapter", "notifyDataSetChanged called")
    }
}