package com.example.doggo.Home

object ProfileManager {
    private val dogProfiles = mutableListOf<DogProfile>()

    fun addProfile(profile: DogProfile) {
        dogProfiles.add(profile)
        android.util.Log.d("ProfileManager", "Profile added: ${profile.name}, Total: ${dogProfiles.size}")
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
    }

    fun removeProfile(profileId: String) {
        dogProfiles.removeAll { it.id == profileId }
    }
}