package com.example.doggo.network

import retrofit2.Call
import retrofit2.http.*

data class SignUpRequest(
    val email: String,
    val password: String
)

data class SignInRequest(
    val email: String,
    val password: String
)

data class AddDogRequest(
    val name: String,
    val breed: String = "",
    val age: Int = 0,
    val birthDate: String = "",
    val photo: String = ""
    // TANPA ownerId - karena diambil dari token
)

data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val userId: Int? = null,
    val userDbId: Int? = null,
    val uid: String? = null,
    val dogId: Int? = null,
    val error: String? = null
)

data class DogsResponse(
    val success: Boolean,
    val dogs: Map<String, DogData> = emptyMap(),
    val error: String? = null
)

data class DogData(
    val dogId: Int,
    val name: String,
    val breed: String,
    val age: Int,
    val birthDate: String,
    val photo: String,
    val ownerId: Int,
    val createdAt: String
)

interface ApiService {
    // Authentication
    @POST("api/signup")
    fun signUp(@Body request: SignUpRequest): Call<ApiResponse>

    @POST("api/signin")
    fun signIn(@Body request: SignInRequest): Call<ApiResponse>

    // Dogs
    @POST("api/dogs")
    fun addDog(@Body request: AddDogRequest): Call<ApiResponse>

    @GET("api/my-dogs")
    fun getMyDogs(): Call<DogsResponse>
}