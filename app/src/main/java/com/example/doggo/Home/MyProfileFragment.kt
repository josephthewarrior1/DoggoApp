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
    private val binding get() = _binding

    private lateinit var sharedPreferences: SharedPreferences
    private var userApiCall: Call<UserResponse>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding!!.root
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
        binding?.apply {
            if (username != null && username != "User") {
                tvUsername.text = username
                tvAvatarInitial.text = username.firstOrNull()?.uppercase() ?: "U"
            } else {
                tvUsername.text = "User"
                tvAvatarInitial.text = "U"
            }
            tvEmail.text = email
        }

        // Then fetch fresh data from API if userId exists
        if (userId != -1) {
            fetchUserFromApi(userId)
        } else {
            Log.w("MyProfileFragment", "⚠️ No user_db_id found in SharedPreferences")
        }
    }

    private fun fetchUserFromApi(userId: Int) {
        Log.d("MyProfileFragment", "🌐 Fetching user data from API for userId: $userId")

        // Cancel previous call if exists
        userApiCall?.cancel()

        userApiCall = RetrofitClient.instance.getUserById(userId)
        userApiCall?.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                // Check if fragment is still attached and binding exists
                if (!isAdded || _binding == null) {
                    Log.w("MyProfileFragment", "⚠️ Fragment detached or binding null, skipping UI update")
                    return
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    user?.let {
                        Log.d("MyProfileFragment", "✅ User data fetched - Username: ${it.username}, Email: ${it.email}")

                        // Update UI with safe binding access
                        binding?.apply {
                            tvEmail.text = it.email
                            tvUsername.text = it.username ?: "No username"
                            tvAvatarInitial.text = (it.username ?: it.email).firstOrNull()?.uppercase() ?: "U"
                        }

                        // Update SharedPreferences with fresh data
                        sharedPreferences.edit().apply {
                            putString("email", it.email)
                            it.username?.let { username -> putString("username", username) }
                            apply()
                        }
                    }
                } else {
                    if (isAdded) {
                        Log.e("MyProfileFragment", "❌ API Error: ${response.code()} - ${response.body()?.error}")
                    }
                }

                // Clear the call reference
                userApiCall = null
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                // Only log if fragment is still attached
                if (isAdded) {
                    // Check if the call was cancelled (which is normal when fragment detaches)
                    if (!call.isCanceled) {
                        Log.e("MyProfileFragment", "❌ Network error: ${t.message}", t)
                    } else {
                        Log.d("MyProfileFragment", "ℹ️ API call cancelled (normal during navigation)")
                    }
                }

                // Clear the call reference
                userApiCall = null
            }
        })
    }

    private fun setupClickListeners() {
        // Use safe calls for all click listeners
        binding?.apply {
            // Edit Profile Button (di dalam card)
            btnEditProfile.setOnClickListener {
                if (isAdded) {
                    val intent = Intent(requireContext(), EditProfileActivity::class.java)
                    startActivity(intent)
                }
            }

            // Account Settings - Edit Profile
            layoutEditProfile.setOnClickListener {
                if (isAdded) {
                    val intent = Intent(requireContext(), EditProfileActivity::class.java)
                    startActivity(intent)
                }
            }

            // App Settings
            layoutPrivacyPolicy.setOnClickListener {
                if (isAdded) {
                    showComingSoonToast("Privacy Policy")
                }
            }

            layoutTermsConditions.setOnClickListener {
                if (isAdded) {
                    showComingSoonToast("Terms & Conditions")
                }
            }

            layoutAboutUs.setOnClickListener {
                if (isAdded) {
                    showComingSoonToast("About Us")
                }
            }

            // Help & Support
            layoutHelp.setOnClickListener {
                if (isAdded) {
                    showComingSoonToast("Help & Support")
                }
            }

            // Logout
            layoutLogout.setOnClickListener {
                if (isAdded) {
                    showLogoutConfirmation()
                }
            }
        }
    }

    private fun showComingSoonToast(feature: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), "$feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmation() {
        if (!isAdded) return

        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                if (isAdded) {
                    performLogout()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        if (!isAdded) return

        // Cancel any pending API calls
        userApiCall?.cancel()

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
        // Cancel any ongoing API calls
        userApiCall?.cancel()
        userApiCall = null

        // Clear binding
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Final cleanup
        userApiCall?.cancel()
        userApiCall = null
    }
}