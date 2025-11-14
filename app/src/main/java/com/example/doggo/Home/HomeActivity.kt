package com.example.doggo.Home

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.R
import com.example.doggo.databinding.ActivityHomeBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.DogsResponse
import com.example.doggo.network.DogData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var dogProfileAdapter: DogProfileAdapter
    private val dogProfiles = mutableListOf<DogProfile>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvUserName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences dan TextView
        sharedPreferences = getSharedPreferences("doggo_pref", MODE_PRIVATE)
        tvUserName = findViewById(R.id.tvUserName) // Pastikan ID ini ada di XML layout

        setupRecyclerView()
        setupUI()
        loadUserData() // ← TAMBAH INI: Load username dari SharedPreferences
        checkProfilesAndUpdateUI()
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI ketika kembali dari AddDogProfileActivity
        checkProfilesAndUpdateUI()
    }

    private fun loadUserData() {
        val username = sharedPreferences.getString("username", null)

        if (!username.isNullOrEmpty()) {
            // Jika username ada, set ke TextView
            tvUserName.text = username
            Log.d("HomeActivity", "✅ Username loaded from SharedPreferences: $username")
        } else {
            // Jika ga ada username, set default
            tvUserName.text = "User"
            Log.d("HomeActivity", "❌ No username found in SharedPreferences, using default")

            // Debug: Check what's actually in SharedPreferences
            val allPrefs = sharedPreferences.all
            Log.d("HomeActivity", "🔍 All SharedPreferences: $allPrefs")
        }
    }

    private fun setupRecyclerView() {
        dogProfileAdapter = DogProfileAdapter(dogProfiles) { dogProfile ->
            navigateToProfileDetail(dogProfile)
        }

        binding.rvDogProfiles.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dogProfileAdapter
        }
    }

    private fun setupUI() {
        binding.btnAddPet.setOnClickListener {
            navigateToAddDogProfile()
        }

        binding.btnAddMore.setOnClickListener {
            navigateToAddDogProfile()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_profiles -> true
                R.id.nav_my_profile -> {
                    // TODO: Navigate to user profile
                    showUserProfile()
                    true
                }
                else -> false
            }
        }

        binding.btnSearch.setOnClickListener {
            // TODO: Implement search functionality
            Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnMenu.setOnClickListener {
            // TODO: Implement menu functionality
            showUserMenu()
        }
    }

    private fun showUserMenu() {
        val username = sharedPreferences.getString("username", "User")
        Toast.makeText(this, "Logged in as: $username", Toast.LENGTH_SHORT).show()
    }

    private fun showUserProfile() {
        val username = sharedPreferences.getString("username", "User")
        val userId = sharedPreferences.getString("user_id", "Unknown")

        Toast.makeText(this, "User Profile: $username (ID: $userId)", Toast.LENGTH_SHORT).show()
    }

    private fun checkProfilesAndUpdateUI() {
        // COBA LOAD DARI API DULU
        loadDogsFromAPI()
    }

    private fun loadDogsFromAPI() {
        Log.d("HomeActivity", "🔄 Loading dogs from API...")

        // Show loading state
        binding.profilesContentLayout.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.profilesContentLayout.alpha = 0.5f

        RetrofitClient.instance.getMyDogs().enqueue(object : Callback<DogsResponse> {
            override fun onResponse(call: Call<DogsResponse>, response: Response<DogsResponse>) {
                binding.profilesContentLayout.alpha = 1.0f

                if (response.isSuccessful) {
                    val dogsResponse = response.body()
                    Log.d("HomeActivity", "✅ API Response: ${dogsResponse?.success}")

                    if (dogsResponse?.success == true) {
                        // ✅ Convert Map to List
                        val dogsMap = dogsResponse.dogs ?: emptyMap()
                        val dogsList = dogsMap.values.toList()

                        Log.d("HomeActivity", "📊 Dogs list size: ${dogsList.size}")

                        // Clear existing profiles
                        dogProfiles.clear()

                        // Convert API response to DogProfile objects
                        dogsList.forEach { dogData ->
                            val profile = DogProfile(
                                id = dogData.dogId.toString(),
                                name = dogData.name,
                                breed = dogData.breed,
                                age = dogData.age,
                                weight = dogData.weight ?: 0.0,  // ✅ FETCH WEIGHT
                                gender = dogData.gender ?: "",   // ✅ FETCH GENDER
                                photoUrl = dogData.photo ?: "",
                                additionalInfo = ""
                            )
                            dogProfiles.add(profile)
                            Log.d("HomeActivity", "🐶 Added: ${dogData.name} (${dogData.breed}) - ${dogData.gender}, ${dogData.weight}kg")
                        }

                        Log.d("HomeActivity", "✅ Total dogs loaded: ${dogProfiles.size}")
                        updateUI()

                    } else {
                        Log.e("HomeActivity", "❌ API error: ${dogsResponse?.error}")
                        // Fallback ke local data
                        loadTemporaryProfiles()
                    }
                } else {
                    Log.e("HomeActivity", "❌ HTTP error: ${response.code()} - ${response.message()}")
                    // Fallback ke local data
                    loadTemporaryProfiles()
                }
            }

            override fun onFailure(call: Call<DogsResponse>, t: Throwable) {
                binding.profilesContentLayout.alpha = 1.0f
                Log.e("HomeActivity", "❌ Network error: ${t.message}")
                Toast.makeText(this@HomeActivity, "Network error", Toast.LENGTH_SHORT).show()
                // Fallback ke local data
                loadTemporaryProfiles()
            }
        })
    }

    private fun loadTemporaryProfiles() {
        // Fallback: Load dari ProfileManager lokal
        dogProfiles.clear()
        val allProfiles = ProfileManager.getAllProfiles()
        dogProfiles.addAll(allProfiles)
        Log.d("HomeActivity", "🔄 Loaded ${dogProfiles.size} profiles from local ProfileManager")
        updateUI()
    }

    private fun updateUI() {
        val profileCount = dogProfiles.size
        Log.d("HomeActivity", "🎯 Updating UI with $profileCount profiles")

        if (profileCount > 0) {
            showProfilesContent()
        } else {
            showEmptyState()
        }
    }

    private fun showEmptyState() {
        Log.d("HomeActivity", "🔭 Showing empty state")
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.profilesContentLayout.visibility = View.GONE
    }

    private fun showProfilesContent() {
        Log.d("HomeActivity", "📊 Showing profiles content")
        binding.emptyStateLayout.visibility = View.GONE
        binding.profilesContentLayout.visibility = View.VISIBLE

        // Update profile count
        binding.tvProfileCount.text = dogProfiles.size.toString()

        // Update adapter
        dogProfileAdapter.updateProfiles(dogProfiles.toList())
        Log.d("HomeActivity", "✅ Adapter updated with ${dogProfiles.size} profiles")
    }

    private fun navigateToAddDogProfile() {
        val intent = Intent(this, AddDogProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToProfileDetail(dogProfile: DogProfile) {
        val intent = Intent(this, DogProfileDetailActivity::class.java)
        intent.putExtra("DOG_PROFILE_ID", dogProfile.id)
        startActivity(intent)
    }
}