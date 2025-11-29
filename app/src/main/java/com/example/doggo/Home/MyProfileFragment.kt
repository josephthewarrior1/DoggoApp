package com.example.doggo.Home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.doggo.Home.profile.ProfileManager
import com.example.doggo.IntroActivity
import com.example.doggo.databinding.FragmentMyProfileBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("doggo_pref", Context.MODE_PRIVATE)

        loadUserInfo()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Reload user info when returning to this fragment
        loadUserInfo()
    }

    private fun loadUserInfo() {
        // First, load from SharedPreferences (for immediate display)
        val username = sharedPreferences.getString("username", null)
        val email = sharedPreferences.getString("email", "user@example.com") ?: "user@example.com"
        val userId = sharedPreferences.getInt("user_db_id", -1)

        Log.d("MyProfileFragment", "📱 Loading cached data - Username: $username, Email: $email, UserId: $userId")

        // Display cached data first
        if (username != null && username != "User") {
            binding.tvUsername.text = username
            binding.tvAvatarInitial.text = username.firstOrNull()?.uppercase() ?: "U"
        } else {
            binding.tvUsername.text = "User"
            binding.tvAvatarInitial.text = "U"
        }
        binding.tvEmail.text = email

        // Then fetch fresh data from API if userId exists
        if (userId != -1) {
            fetchUserFromApi(userId)
        } else {
            Log.w("MyProfileFragment", "⚠️ No user_db_id found in SharedPreferences")
        }
    }

    private fun fetchUserFromApi(userId: Int) {
        Log.d("MyProfileFragment", "🌐 Fetching user data from API for userId: $userId")

        RetrofitClient.instance.getUserById(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    user?.let {
                        Log.d("MyProfileFragment", "✅ User data fetched - Username: ${it.username}, Email: ${it.email}")

                        // Update UI
                        binding.tvEmail.text = it.email
                        binding.tvUsername.text = it.username ?: "No username"
                        binding.tvAvatarInitial.text = (it.username ?: it.email).firstOrNull()?.uppercase() ?: "U"

                        // Update SharedPreferences with fresh data
                        sharedPreferences.edit().apply {
                            putString("email", it.email)
                            it.username?.let { username -> putString("username", username) }
                            apply()
                        }
                    }
                } else {
                    Log.e("MyProfileFragment", "❌ API Error: ${response.code()} - ${response.body()?.error}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // Silent fail - we already have cached data displayed
                Log.e("MyProfileFragment", "❌ Network error: ${t.message}", t)
            }
        })
    }

    private fun setupClickListeners() {
        // Account Settings
        binding.layoutEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.layoutChangePassword.setOnClickListener {
            showComingSoonToast("Change Password")
        }

        binding.layoutNotifications.setOnClickListener {
            showComingSoonToast("Notifications Settings")
        }

        // App Settings
        binding.layoutPrivacyPolicy.setOnClickListener {
            showComingSoonToast("Privacy Policy")
        }

        binding.layoutTermsConditions.setOnClickListener {
            showComingSoonToast("Terms & Conditions")
        }

        binding.layoutAboutUs.setOnClickListener {
            showComingSoonToast("About Us")
        }

        // Logout
        binding.layoutLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showComingSoonToast(feature: String) {
        Toast.makeText(requireContext(), "$feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear shared preferences
        sharedPreferences.edit().clear().apply()

        // Clear ProfileManager
        ProfileManager.clearProfiles()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate back to IntroActivity
        val intent = Intent(requireActivity(), IntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}