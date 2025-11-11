package com.example.doggo.Home

data class DogProfile(
    val id: String = "",
    val name: String = "",
    val breed: String = "",
    val age: Int = 0,
    val gender: String = "",
    val weight: Double = 0.0,
    val photoUrl: String = "",
    val additionalInfo: String = ""
)