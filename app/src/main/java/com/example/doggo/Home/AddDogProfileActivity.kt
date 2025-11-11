package com.example.doggo.Home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.R
import com.example.doggo.databinding.ActivityAddDogProfileBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.AddDogRequest
import com.example.doggo.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // Show loading
        Toast.makeText(this, "Saving dog profile...", Toast.LENGTH_SHORT).show()

        // ✅ PAKAI REQUEST TANPA ownerId
        val addDogRequest = AddDogRequest(
            name = name,
            breed = breed,
            age = age
            // birthDate dan photo bisa dikosongin dulu
        )

        // Send to backend
        RetrofitClient.instance.addDog(addDogRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        Toast.makeText(
                            this@AddDogProfileActivity,
                            "Dog profile saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddDogProfileActivity,
                            apiResponse?.error ?: "Failed to save dog profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@AddDogProfileActivity,
                        "Failed to save: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(
                    this@AddDogProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}