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
            // TODO: Implement profile details view
        }

        binding.rvDogProfiles.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
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
        // TODO: Load profiles from database
        // For now, we'll check if the list is empty
        loadTemporaryProfiles() // Temporary - will be replaced with database call

        if (dogProfiles.isEmpty()) {
            showEmptyState()
        } else {
            showProfilesContent()
        }
    }

    private fun loadTemporaryProfiles() {
        // TODO: Replace this with actual database loading
        // Load from ProfileManager
        dogProfiles.clear()
        dogProfiles.addAll(ProfileManager.getAllProfiles())
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.profilesContentLayout.visibility = View.GONE
    }

    private fun showProfilesContent() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.profilesContentLayout.visibility = View.VISIBLE

        // Update profile count
        binding.tvProfileCount.text = dogProfiles.size.toString()

        // Update adapter
        dogProfileAdapter.updateProfiles(dogProfiles)
    }

    private fun navigateToAddDogProfile() {
        val intent = Intent(this, AddDogProfileActivity::class.java)
        startActivity(intent)
    }

    // Temporary method to add a profile (for testing)
    // TODO: Remove this and use database
    fun addDogProfileTemp(profile: DogProfile) {
        dogProfiles.add(profile)
        checkProfilesAndUpdateUI()
    }
}