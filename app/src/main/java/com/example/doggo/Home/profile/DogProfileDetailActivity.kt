package com.example.doggo.Home.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.doggo.Home.details.DogDetailPagerAdapter
import com.example.doggo.Home.profile.EditDogProfileActivity
import com.example.doggo.Home.profile.ProfileManager
import com.example.doggo.R
import com.example.doggo.databinding.ActivityDogProfileDetailTabsBinding
import com.example.doggo.network.DogResponse
import com.example.doggo.network.RetrofitClient
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DogProfileDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDogProfileDetailTabsBinding
    private var dogProfile: DogProfile? = null
    private var pagerAdapter: DogDetailPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogProfileDetailTabsBinding.inflate(layoutInflater)
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
                        Log.d("DogProfileDetail", "📅 Schedule data: ${dogData.schedule}")

                        dogProfile = DogProfile(
                            id = dogData.dogId.toString(),
                            name = dogData.name,
                            breed = dogData.breed,
                            age = dogData.age,
                            weight = dogData.weight ?: 0.0,
                            gender = dogData.gender ?: "",
                            photoUrl = dogData.photo ?: "",
                            additionalInfo = "",
                            schedule = dogData.schedule
                        )

                        displayDogProfile(dogProfile!!)
                        setupViewPager(dogProfile!!)

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
            setupViewPager(dogProfile!!)
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

            // Load photo using Glide
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

        Log.d("DogProfileDetail", "📱 Displaying: ${profile.name} - ${profile.breed}")
    }

    private fun setupViewPager(profile: DogProfile) {
        pagerAdapter = DogDetailPagerAdapter(this, profile)
        binding.viewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager2 - Hanya 2 tab
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Info"
                1 -> "Medical"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditDogProfileActivity::class.java)
            intent.putExtra("DOG_ID", dogProfile?.id?.toIntOrNull() ?: -1)
            startActivity(intent)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
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

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
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

    override fun onResume() {
        super.onResume()
        // Reload profile when returning from edit
        dogProfile?.id?.let { loadDogFromAPI(it) }
    }
}