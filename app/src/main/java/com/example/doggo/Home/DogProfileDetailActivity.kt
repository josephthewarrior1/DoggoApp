package com.example.doggo.Home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.databinding.ActivityDogProfileDetailBinding

class DogProfileDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDogProfileDetailBinding
    private var dogProfile: DogProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogProfileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadDogProfile()
        setupUI()
    }

    private fun loadDogProfile() {
        val profileId = intent.getStringExtra("DOG_PROFILE_ID")

        if (profileId != null) {
            // Get profile from ProfileManager
            dogProfile = ProfileManager.getAllProfiles().find { it.id == profileId }

            if (dogProfile != null) {
                displayDogProfile(dogProfile!!)
            } else {
                Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, "Invalid profile", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayDogProfile(profile: DogProfile) {
        binding.apply {
            tvDogName.text = profile.name
            tvBreed.text = profile.breed
            tvAgeValue.text = profile.age.toString()
            tvGenderValue.text = profile.gender
            tvWeightValue.text = String.format("%.1f", profile.weight)

            if (profile.additionalInfo.isNotEmpty()) {
                tvAdditionalInfo.text = profile.additionalInfo
            } else {
                tvAdditionalInfo.text = "No additional information provided"
            }

            // TODO: Load photo from URL when image storage is implemented
        }
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            // TODO: Navigate to edit profile screen
            Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Delete Profile")
        builder.setMessage("Are you sure you want to delete ${dogProfile?.name}'s profile? This action cannot be undone.")

        builder.setPositiveButton("Delete") { dialog, _ ->
            deleteProfile()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        // Customize button colors
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(getColor(android.R.color.holo_red_dark))
    }

    private fun deleteProfile() {
        dogProfile?.let { profile ->
            // Remove from ProfileManager
            ProfileManager.removeProfile(profile.id)

            Toast.makeText(
                this,
                "${profile.name}'s profile has been deleted",
                Toast.LENGTH_SHORT
            ).show()

            // Close this activity and return to home
            finish()
        }
    }
}