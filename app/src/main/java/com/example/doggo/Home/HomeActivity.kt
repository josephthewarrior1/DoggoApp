package com.example.doggo.Home

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.R
import com.example.doggo.databinding.ActivityHomeBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.DogsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var dogProfileAdapter: DogProfileAdapter
    private val dogProfiles = mutableListOf<DogProfile>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("doggo_pref", MODE_PRIVATE)

        setupRecyclerView()
        setupUI()
        loadUserData()
        checkProfilesAndUpdateUI()

        // Handle notification click
        handleNotificationIntent()
    }

    override fun onResume() {
        super.onResume()
        checkProfilesAndUpdateUI()
    }

    private fun handleNotificationIntent() {
        val openRemindersTab = intent.getBooleanExtra("OPEN_REMINDERS_TAB", false)
        if (openRemindersTab) {
            binding.bottomNavigation.selectedItemId = R.id.nav_reminders
            showReminderFragment()
            Log.d("HomeActivity", "📲 Opened from notification - showing Reminders tab")
        }
    }

    private fun loadUserData() {
        val username = sharedPreferences.getString("username", null)

        if (!username.isNullOrEmpty()) {
            binding.tvGreeting.text = "Hi $username"
            Log.d("HomeActivity", "✅ Username loaded from SharedPreferences: $username")
        } else {
            binding.tvGreeting.text = "Hi User"
            Log.d("HomeActivity", "❌ No username found in SharedPreferences, using default")
        }

        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greetingText = when (currentHour) {
            in 0..11 -> "Good Morning!"
            in 12..17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }
        binding.tvSubGreeting.text = greetingText
    }

    private fun setupRecyclerView() {
        dogProfileAdapter = DogProfileAdapter(dogProfiles) { dogProfile ->
            navigateToProfileDetail(dogProfile)
        }

        binding.rvDogProfiles.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.VERTICAL, false)
            adapter = dogProfileAdapter
        }
    }

    private fun setupUI() {
        binding.btnAddYourPet.setOnClickListener {
            navigateToAddDogProfile()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomeContent()
                    true
                }
                R.id.nav_reminders -> {
                    showReminderFragment()
                    true
                }
                R.id.nav_account -> {
                    showProfileFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun showHomeContent() {
        binding.mainContentLayout.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        hideFragment()
        Log.d("HomeActivity", "🏠 Showing Home content")
    }

    private fun showReminderFragment() {
        binding.mainContentLayout.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (existingFragment !is ReminderFragment) {
            val fragment = ReminderFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            Log.d("HomeActivity", "📋 Showing Reminder fragment")
        }
    }

    private fun showProfileFragment() {
        binding.mainContentLayout.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        val existingFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (existingFragment !is MyProfileFragment) {
            val fragment = MyProfileFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            Log.d("HomeActivity", "👤 Showing Profile fragment")
        }
    }

    private fun hideFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
            Log.d("HomeActivity", "🗑️ Fragment removed")
        }
    }

    private fun showUserProfile() {
        val username = sharedPreferences.getString("username", "User")
        val userId = sharedPreferences.getString("user_id", "Unknown")
        Toast.makeText(this, "User Profile: $username (ID: $userId)", Toast.LENGTH_SHORT).show()
    }

    private fun checkProfilesAndUpdateUI() {
        loadDogsFromAPI()
    }

    private fun loadDogsFromAPI() {
        Log.d("HomeActivity", "📄 Loading dogs from API...")

        binding.mainContentLayout.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.mainContentLayout.alpha = 0.5f

        RetrofitClient.instance.getMyDogs().enqueue(object : Callback<DogsResponse> {
            override fun onResponse(call: Call<DogsResponse>, response: Response<DogsResponse>) {
                binding.mainContentLayout.alpha = 1.0f

                if (response.isSuccessful) {
                    val dogsResponse = response.body()
                    Log.d("HomeActivity", "✅ API Response: ${dogsResponse?.success}")

                    if (dogsResponse?.success == true) {
                        val dogsMap = dogsResponse.dogs ?: emptyMap()
                        val dogsList = dogsMap.values.toList()

                        Log.d("HomeActivity", "📊 Dogs list size: ${dogsList.size}")

                        dogProfiles.clear()

                        dogsList.forEach { dogData ->
                            val profile = DogProfile(
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
                            dogProfiles.add(profile)
                            Log.d("HomeActivity", "🐶 Added: ${dogData.name} (${dogData.breed})")
                        }

                        Log.d("HomeActivity", "✅ Total dogs loaded: ${dogProfiles.size}")
                        updateUI()

                    } else {
                        Log.e("HomeActivity", "❌ API error: ${dogsResponse?.error}")
                        loadTemporaryProfiles()
                    }
                } else {
                    Log.e("HomeActivity", "❌ HTTP error: ${response.code()} - ${response.message()}")
                    loadTemporaryProfiles()
                }
            }

            override fun onFailure(call: Call<DogsResponse>, t: Throwable) {
                binding.mainContentLayout.alpha = 1.0f
                Log.e("HomeActivity", "❌ Network error: ${t.message}")
                Toast.makeText(this@HomeActivity, "Network error", Toast.LENGTH_SHORT).show()
                loadTemporaryProfiles()
            }
        })
    }

    private fun loadTemporaryProfiles() {
        dogProfiles.clear()
        val allProfiles = ProfileManager.getAllProfiles()
        dogProfiles.addAll(allProfiles)
        Log.d("HomeActivity", "📄 Loaded ${dogProfiles.size} profiles from local ProfileManager")
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
        binding.mainContentLayout.visibility = View.GONE
    }

    private fun showProfilesContent() {
        Log.d("HomeActivity", "📊 Showing profiles content")
        binding.emptyStateLayout.visibility = View.GONE
        binding.mainContentLayout.visibility = View.VISIBLE

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