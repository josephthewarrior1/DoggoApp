package com.example.doggo.network

import retrofit2.Call
import retrofit2.http.*
import java.io.Serializable

data class SignUpRequest(
    val email: String,
    val password: String,
    val username: String
)

data class SignInRequest(
    val email: String,
    val password: String
)

data class AddDogRequest(
    val name: String,
    val breed: String = "",
    val age: Int = 0,
    val weight: Double? = null,
    val gender: String? = null,
    val birthDate: String = "",
    val photo: String = "",
    val schedule: DogSchedule? = null
)

// ✅ TAMBAH: Serializable
data class DogSchedule(
    val eat: List<ScheduleDetail>? = null,
    val walk: List<ScheduleDetail>? = null,
    val sleep: List<ScheduleDetail>? = null,
    val medicine: List<ScheduleDetail>? = null,
    val groom: List<ScheduleDetail>? = null
) : Serializable

// ✅ TAMBAH: Serializable
data class ScheduleDetail(
    val id: String? = null,
    val time: String = "",
    val description: String = "",
    val duration: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Serializable

// Schedule detail dengan ID (untuk response dari API)
data class ScheduleDetailWithId(
    val id: String,
    val time: String,
    val description: String,
    val createdAt: String
) : Serializable

data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val userId: Int? = null,
    val userDbId: Int? = null,
    val uid: String? = null,
    val dogId: Int? = null,
    val username: String? = null,
    val error: String? = null
)

// Changed from List to Map
data class DogsResponse(
    val success: Boolean,
    val dogs: Map<String, DogData>? = emptyMap(),
    val error: String? = null
)

// Response for single dog
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
    val weight: Double?,
    val gender: String?,
    val createdAt: String,
    val schedule: DogSchedule? = null
)

// Request untuk add/update/delete schedule
data class ScheduleRequest(
    val scheduleType: String,
    val time: String,
    val description: String = ""
)

data class ScheduleUpdateRequest(
    val scheduleType: String,
    val scheduleItemId: String,
    val time: String? = null,
    val description: String? = null
)

data class ScheduleDeleteRequest(
    val scheduleType: String,
    val scheduleItemId: String
)

data class ScheduleResponse(
    val success: Boolean,
    val message: String? = null,
    val scheduleType: String? = null,
    val scheduleItem: ScheduleDetailWithId? = null,
    val scheduleItemId: String? = null,
    val dogId: Int? = null,
    val error: String? = null
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

    @GET("api/dogs/{id}")
    fun getDogById(@Path("id") dogId: String): Call<DogResponse>

    @PUT("api/dogs/{id}")
    fun updateDog(
        @Path("id") dogId: Int,
        @Body request: AddDogRequest
    ): Call<ApiResponse>

    // Schedule endpoints
    @POST("api/dogs/{id}/schedule")
    fun addSchedule(
        @Path("id") dogId: String,
        @Body request: ScheduleRequest
    ): Call<ScheduleResponse>

    @PUT("api/dogs/{id}/schedule")
    fun updateSchedule(
        @Path("id") dogId: String,
        @Body request: ScheduleUpdateRequest
    ): Call<ScheduleResponse>

    @HTTP(method = "DELETE", path = "api/dogs/{id}/schedule", hasBody = true)
    fun deleteSchedule(
        @Path("id") dogId: String,
        @Body request: ScheduleDeleteRequest
    ): Call<ScheduleResponse>
}