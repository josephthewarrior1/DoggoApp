package com.example.doggo.Home

import android.util.Log

object ProfileManager {
    private val dogProfiles = mutableListOf<DogProfile>()

    fun addProfile(profile: DogProfile) {
        dogProfiles.add(profile)
        Log.d("ProfileManager", "Profile added: ${profile.name}, Total: ${dogProfiles.size}")
    }

    fun getAllProfiles(): List<DogProfile> {
        return dogProfiles.toList()
    }

    fun getProfileById(id: String): DogProfile? {
        return dogProfiles.find { it.id == id }
    }

    fun getProfileCount(): Int {
        return dogProfiles.size
    }

    fun clearProfiles() {
        dogProfiles.clear()
        Log.d("ProfileManager", "All profiles cleared")
    }

    fun removeProfile(profileId: String) {
        val removed = dogProfiles.removeAll { it.id == profileId }
        Log.d("ProfileManager", "Profile $profileId removed: $removed, Remaining: ${dogProfiles.size}")
    }
}