package com.example.doggo.Home

object ProfileManager {
    private val dogProfiles = mutableListOf<DogProfile>()

    fun addProfile(profile: DogProfile) {
        dogProfiles.add(profile)
    }

    fun getAllProfiles(): List<DogProfile> {
        return dogProfiles.toList()
    }

    fun getProfileCount(): Int {
        return dogProfiles.size
    }

    fun clearProfiles() {
        dogProfiles.clear()
    }

    fun removeProfile(profileId: String) {
        dogProfiles.removeAll { it.id == profileId }
    }
}