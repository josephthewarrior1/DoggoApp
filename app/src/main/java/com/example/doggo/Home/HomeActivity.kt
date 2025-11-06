package com.example.doggo.Home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.R
import com.example.doggo.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var hasDogProfiles = false // TODO: This will be checked from database later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkProfilesAndUpdateUI()
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI when returning from AddDogProfileActivity
        checkProfilesAndUpdateUI()
    }

    private fun setupUI() {
        // Swipe to Continue button click
        binding.btnSwipeToContinue.setOnClickListener {
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
        // TODO: Check if user has dog profiles from database
        // For now, we're using the hasDogProfiles variable

        if (hasDogProfiles) {
            showProfilesContent()
        } else {
            showEmptyState()
        }
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
    }

    private fun showProfilesContent() {
        binding.emptyStateLayout.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        // TODO: Load DogProfilesFragment
        // Example:
        // supportFragmentManager.beginTransaction()
        //     .replace(R.id.fragmentContainer, DogProfilesFragment())
        //     .commit()
    }

    private fun navigateToAddDogProfile() {
        val intent = Intent(this, AddDogProfileActivity::class.java)
        startActivity(intent)
    }
}