package com.example.doggo.Home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doggo.R
import com.example.doggo.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var dogProfileAdapter: DogProfileAdapter
    private val dogProfiles = mutableListOf<DogProfile>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupUI()
        checkProfilesAndUpdateUI()
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI when returning from AddDogProfileActivity
        // TODO: Load profiles from database
        checkProfilesAndUpdateUI()
    }

    private fun setupRecyclerView() {
        dogProfileAdapter = DogProfileAdapter(dogProfiles) { dogProfile ->
            // Handle profile click - navigate to profile details
            navigateToProfileDetail(dogProfile)
        }

        binding.rvDogProfiles.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dogProfileAdapter
        }
    }

    private fun setupUI() {
        // Add Profile button (empty state)
        binding.btnAddProfile.setOnClickListener {
            navigateToAddDogProfile()
        }

        // Add More button (when profiles exist)
        binding.btnAddMore.setOnClickListener {
            navigateToAddDogProfile()
        }

        // Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_profiles -> {
                    // Navigate to profiles fragment
                    // TODO: Implement navigation
                    true
                }
                R.id.nav_my_profile -> {
                    // Navigate to my profile fragment
                    // TODO: Implement navigation
                    true
                }
                else -> false
            }
        }

        // Search button
        binding.btnSearch.setOnClickListener {
            // TODO: Implement search functionality
        }

        // Menu button
        binding.btnMenu.setOnClickListener {
            // TODO: Implement menu functionality
        }
    }

    private fun checkProfilesAndUpdateUI() {
        // Load profiles from ProfileManager
        loadTemporaryProfiles()

        val profileCount = dogProfiles.size
        android.util.Log.d("HomeActivity", "Number of profiles: $profileCount")
        android.util.Log.d("HomeActivity", "isEmpty check: ${dogProfiles.isEmpty()}")

        if (profileCount > 0) {
            android.util.Log.d("HomeActivity", "Will show profiles content")
            showProfilesContent()
        } else {
            android.util.Log.d("HomeActivity", "Will show empty state")
            showEmptyState()
        }
    }

    private fun loadTemporaryProfiles() {
        // Load from ProfileManager
        dogProfiles.clear()
        android.util.Log.d("HomeActivity", "dogProfiles cleared, size: ${dogProfiles.size}")

        val allProfiles = ProfileManager.getAllProfiles()
        android.util.Log.d("HomeActivity", "Loaded from ProfileManager: ${allProfiles.size} profiles")

        dogProfiles.addAll(allProfiles)
        android.util.Log.d("HomeActivity", "dogProfiles list now has: ${dogProfiles.size} profiles")

        // Log each profile for debugging
        dogProfiles.forEachIndexed { index, profile ->
            android.util.Log.d("HomeActivity", "Profile $index: ${profile.name}, ${profile.breed}")
        }
    }

    private fun showEmptyState() {
        android.util.Log.d("HomeActivity", "Showing empty state")
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.profilesContentLayout.visibility = View.GONE
    }

    private fun showProfilesContent() {
        android.util.Log.d("HomeActivity", "Showing profiles content")
        binding.emptyStateLayout.visibility = View.GONE
        binding.profilesContentLayout.visibility = View.VISIBLE

        // Update profile count
        binding.tvProfileCount.text = dogProfiles.size.toString()

        // Log before updating adapter
        android.util.Log.d("HomeActivity", "About to update adapter with ${dogProfiles.size} profiles")
        dogProfiles.forEach { profile ->
            android.util.Log.d("HomeActivity", "Profile in list: ${profile.name}")
        }

        // Update adapter with a copy of the list
        dogProfileAdapter.updateProfiles(dogProfiles.toList())
        android.util.Log.d("HomeActivity", "Adapter updated with ${dogProfiles.size} profiles")
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

    // Temporary method to add a profile (for testing)
    // TODO: Remove this and use database
    fun addDogProfileTemp(profile: DogProfile) {
        dogProfiles.add(profile)
        checkProfilesAndUpdateUI()
    }
}