package com.example.doggo.Home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.databinding.ActivityEditProfileBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.UpdateUserRequest
import com.example.doggo.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUsername: String = ""
    private var currentEmail: String = ""
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("doggo_pref", Context.MODE_PRIVATE)

        loadCurrentUserInfo()
        setupClickListeners()
    }

    private fun loadCurrentUserInfo() {
        currentUsername = sharedPreferences.getString("username", "") ?: ""
        currentEmail = sharedPreferences.getString("email", "") ?: ""
        userId = sharedPreferences.getInt("user_db_id", -1)

        // Display current info
        binding.tvCurrentUsername.text = "Username: $currentUsername"
        binding.tvCurrentEmail.text = "Email: $currentEmail"
        binding.tvEmailDisplay.text = currentEmail
        binding.tvAvatarInitial.text = currentUsername.firstOrNull()?.uppercase() ?: "U"

        Log.d("EditProfile", "📱 Loaded user info - ID: $userId, Username: $currentUsername, Email: $currentEmail")
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSaveChanges.setOnClickListener {
            validateAndSaveChanges()
        }
    }

    private fun validateAndSaveChanges() {
        val newUsername = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validation: Password is required
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required to save changes", Toast.LENGTH_SHORT).show()
            binding.etPassword.error = "Required"
            return
        }

        // Check if any changes were made
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate username length if provided
        if (newUsername.length < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
            binding.etUsername.error = "Too short"
            return
        }

        // Show confirmation dialog
        showConfirmationDialog(newUsername, password)
    }

    private fun showConfirmationDialog(newUsername: String, password: String) {
        val message = "Are you sure you want to update:\n\nUsername: $currentUsername → $newUsername"

        AlertDialog.Builder(this)
            .setTitle("Confirm Changes")
            .setMessage(message)
            .setPositiveButton("Save") { _, _ ->
                updateProfile(newUsername, password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfile(newUsername: String, password: String) {
        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        binding.btnSaveChanges.isEnabled = false
        binding.btnSaveChanges.text = "Saving..."

        // Build request body - email is always null (disabled)
        val requestBody = UpdateUserRequest(
            username = newUsername,
            email = null,
            password = password
        )

        Log.d("EditProfile", "🌐 Updating profile with data: $requestBody")

        RetrofitClient.instance.updateUser(userId, requestBody).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save Changes"

                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedUser = response.body()?.user

                    Log.d("EditProfile", "✅ Profile updated successfully")

                    // Update SharedPreferences
                    sharedPreferences.edit().apply {
                        updatedUser?.username?.let { putString("username", it) }
                        apply()
                    }

                    Toast.makeText(
                        this@EditProfileActivity,
                        "Profile updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Go back to profile
                    finish()
                } else {
                    val errorMsg = response.body()?.error ?: "Failed to update profile"
                    Log.e("EditProfile", "❌ Error: $errorMsg")

                    Toast.makeText(
                        this@EditProfileActivity,
                        errorMsg,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save Changes"

                Log.e("EditProfile", "❌ Network error: ${t.message}", t)

                Toast.makeText(
                    this@EditProfileActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}