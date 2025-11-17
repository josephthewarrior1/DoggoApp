package com.example.doggo.Home

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.doggo.R
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
            loadDogFromAPI(profileId)
        } else {
            Toast.makeText(this, "Invalid profile ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadDogFromAPI(profileId: String) {
        Log.d("DogProfileDetail", "📡 Loading dog from API...")

        RetrofitClient.instance.getDogById(profileId).enqueue(object : Callback<DogResponse> {
            override fun onResponse(call: Call<DogResponse>, response: Response<DogResponse>) {
                if (response.isSuccessful) {
                    val dogResponse = response.body()

                    if (dogResponse?.success == true && dogResponse.dog != null) {
                        val dogData = dogResponse.dog
                        Log.d("DogProfileDetail", "✅ Dog found in API: ${dogData.name}")

                        dogProfile = DogProfile(
                            id = dogData.dogId.toString(),
                            name = dogData.name,
                            breed = dogData.breed,
                            age = dogData.age,
                            weight = dogData.weight ?: 0.0,
                            gender = dogData.gender ?: "",
                            photoUrl = dogData.photo ?: "",
                            additionalInfo = ""
                        )

                        displayDogProfile(dogProfile!!)

                    } else {
                        Log.e("DogProfileDetail", "❌ Dog not found in API, trying local...")
                        loadDogFromLocal(profileId)
                    }
                } else {
                    Log.e("DogProfileDetail", "❌ HTTP error: ${response.code()}")
                    loadDogFromLocal(profileId)
                }
            }

            override fun onFailure(call: Call<DogResponse>, t: Throwable) {
                Log.e("DogProfileDetail", "❌ Network error: ${t.message}")
                loadDogFromLocal(profileId)
            }
        })
    }

    private fun loadDogFromLocal(profileId: String) {
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

            // ✅ Load photo using Glide
            if (profile.photoUrl.isNotEmpty()) {
                Log.d("DogProfileDetail", "🖼️ Loading photo from: ${profile.photoUrl}")

                Glide.with(this@DogProfileDetailActivity)
                    .load(profile.photoUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_dog_placeholder)
                    .error(R.drawable.ic_dog_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivDogPhoto)
            } else {
                Log.d("DogProfileDetail", "📷 No photo URL, using placeholder")
                ivDogPhoto.setImageResource(R.drawable.ic_dog_placeholder)
            }
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

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(getColor(android.R.color.holo_red_dark))
    }

    private fun deleteProfile() {
        dogProfile?.let { profile ->
            // TODO: Implement API delete
            ProfileManager.removeProfile(profile.id)

            Toast.makeText(
                this,
                "${profile.name}'s profile has been deleted",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}