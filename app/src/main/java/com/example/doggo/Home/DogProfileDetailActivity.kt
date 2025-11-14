package com.example.doggo.Home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.databinding.ActivityDogProfileDetailBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.DogResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            Log.d("DogProfileDetail", "🔍 Loading dog with ID: $profileId")

            // ✅ COBA LOAD DARI API DULU
            loadDogFromAPI(profileId)
        } else {
            Toast.makeText(this, "Invalid profile ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadDogFromAPI(profileId: String) {
        Log.d("DogProfileDetail", "🔄 Loading dog from API...")

        RetrofitClient.instance.getDogById(profileId).enqueue(object : Callback<DogResponse> {
            override fun onResponse(call: Call<DogResponse>, response: Response<DogResponse>) {
                if (response.isSuccessful) {
                    val dogResponse = response.body()

                    if (dogResponse?.success == true && dogResponse.dog != null) {
                        val dogData = dogResponse.dog
                        Log.d("DogProfileDetail", "✅ Dog found in API: ${dogData.name}")

                        // Convert API data to DogProfile
                        dogProfile = DogProfile(
                            id = dogData.dogId.toString(),
                            name = dogData.name,
                            breed = dogData.breed,
                            age = dogData.age,
                            weight = dogData.weight ?: 0.0,      // ✅ FETCH WEIGHT
                            gender = dogData.gender ?: "",       // ✅ FETCH GENDER
                            photoUrl = dogData.photo ?: "",
                            additionalInfo = "" // Backend belum ada additionalInfo
                        )

                        displayDogProfile(dogProfile!!)

                    } else {
                        Log.e("DogProfileDetail", "❌ Dog not found in API, trying local...")
                        // Fallback ke ProfileManager lokal
                        loadDogFromLocal(profileId)
                    }
                } else {
                    Log.e("DogProfileDetail", "❌ HTTP error: ${response.code()}")
                    // Fallback ke ProfileManager lokal
                    loadDogFromLocal(profileId)
                }
            }

            override fun onFailure(call: Call<DogResponse>, t: Throwable) {
                Log.e("DogProfileDetail", "❌ Network error: ${t.message}")
                // Fallback ke ProfileManager lokal
                loadDogFromLocal(profileId)
            }
        })
    }

    private fun loadDogFromLocal(profileId: String) {
        // Fallback: Get profile from local ProfileManager
        dogProfile = ProfileManager.getAllProfiles().find { it.id == profileId }

        if (dogProfile != null) {
            Log.d("DogProfileDetail", "✅ Dog found in local: ${dogProfile!!.name}")
            displayDogProfile(dogProfile!!)
        } else {
            Log.e("DogProfileDetail", "❌ Dog not found anywhere")
            Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayDogProfile(profile: DogProfile) {
        binding.apply {
            tvDogName.text = profile.name
            tvBreed.text = profile.breed
            tvAgeValue.text = profile.age.toString()
            tvGenderValue.text = if (profile.gender.isNotEmpty()) profile.gender else "Not specified"
            tvWeightValue.text = if (profile.weight > 0) String.format("%.1f kg", profile.weight) else "Not specified"

            if (profile.additionalInfo.isNotEmpty()) {
                tvAdditionalInfo.text = profile.additionalInfo
            } else {
                tvAdditionalInfo.text = "No additional information provided"
            }

            // TODO: Load photo from URL when image storage is implemented
            // For now, use placeholder
        }

        Log.d("DogProfileDetail", "📱 Displaying: ${profile.name} - ${profile.breed} - ${profile.gender} - ${profile.weight}kg")
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
            // TODO: Implement API delete
            // Untuk sekarang, hapus dari ProfileManager lokal dulu
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