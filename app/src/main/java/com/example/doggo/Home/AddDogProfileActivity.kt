package com.example.doggo.Home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.R
import com.example.doggo.databinding.ActivityAddDogProfileBinding

class AddDogProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDogProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDogProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Back button click
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Photo click listener
        binding.cvProfilePhoto.setOnClickListener {
            // TODO: Open image picker
            Toast.makeText(this, "Image picker coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Save button click
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveDogProfile()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate dog name
        if (binding.etDogName.text.isNullOrBlank()) {
            binding.tilDogName.error = "Please enter dog's name"
            isValid = false
        } else {
            binding.tilDogName.error = null
        }

        // Validate breed
        if (binding.etBreed.text.isNullOrBlank()) {
            binding.tilBreed.error = "Please enter breed"
            isValid = false
        } else {
            binding.tilBreed.error = null
        }

        // Validate age
        if (binding.etAge.text.isNullOrBlank()) {
            binding.tilAge.error = "Please enter age"
            isValid = false
        } else {
            binding.tilAge.error = null
        }

        // Validate weight
        if (binding.etWeight.text.isNullOrBlank()) {
            binding.tilWeight.error = "Please enter weight"
            isValid = false
        } else {
            binding.tilWeight.error = null
        }

        return isValid
    }

    private fun saveDogProfile() {
        // Get all input values
        val name = binding.etDogName.text.toString()
        val breed = binding.etBreed.text.toString()
        val age = binding.etAge.text.toString().toIntOrNull() ?: 0
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0
        val gender = if (binding.rbMale.isChecked) "Male" else "Female"
        val additionalInfo = binding.etAdditionalInfo.text.toString()

        // Create DogProfile object
        val dogProfile = DogProfile(
            id = System.currentTimeMillis().toString(), // Temporary ID
            name = name,
            breed = breed,
            age = age,
            gender = gender,
            weight = weight,
            additionalInfo = additionalInfo
        )

        // TODO: Save to database
        // For now, save to ProfileManager for temporary access
        ProfileManager.addProfile(dogProfile)

        Toast.makeText(
            this,
            "Profile saved! $name added successfully",
            Toast.LENGTH_SHORT
        ).show()

        // Return to previous screen
        finish()
    }
}