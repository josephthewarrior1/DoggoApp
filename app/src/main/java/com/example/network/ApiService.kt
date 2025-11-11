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
    val dogs: List<DogData?>? = emptyList(),
    val error: String? = null
)

// ✅ NEW: Response untuk single dog
data class DogResponse(
    val success: Boolean,
    val dog: DogData? = null,
    val error: String? = null
)

data class DogData(
    val dogId: Int,
    val name: String,
    val breed: String,
    val age: Int,
    val birthDate: String?,
    val photo: String?,
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

    // ✅ NEW: Get single dog by ID
    @GET("api/dogs/{id}")
    fun getDogById(@Path("id") dogId: String): Call<DogResponse>
}