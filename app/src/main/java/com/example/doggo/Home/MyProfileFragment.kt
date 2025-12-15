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
import com.bumptech.glide.Glide
import com.example.doggo.Home.profile.ProfileManager
import com.example.doggo.IntroActivity
import com.example.doggo.databinding.FragmentMyProfileBinding
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.User
import com.example.doggo.network.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyProfileFragment : Fragment() {

    private var _binding: FragmentMyProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private var userApiCall: Call<UserResponse>? = null

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

        setupClickListeners()
        loadUserInfo()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val username = sharedPreferences.getString("username", null)
        val email = sharedPreferences.getString("email", "user@example.com") ?: "user@example.com"
        val userId = sharedPreferences.getInt("user_db_id", -1)

        Log.d("MyProfileFragment", "📱 Cached -> username=$username email=$email userId=$userId")

        // show cached first (fast UI)
        val initial = (username ?: email).firstOrNull()?.uppercase() ?: "U"
        binding.tvUsername.text = username ?: "User"
        binding.tvEmail.text = email
        showInitial(initial)

        // fetch from API
        if (userId != -1) fetchUserFromApi(userId) else Log.w("MyProfileFragment", "⚠️ user_db_id missing")
    }

    private fun fetchUserFromApi(userId: Int) {
        Log.d("MyProfileFragment", "🌐 Fetching userId=$userId")
        userApiCall?.cancel()

        userApiCall = RetrofitClient.instance.getUserById(userId)
        userApiCall?.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!isAdded || _binding == null) return

                if (!response.isSuccessful || response.body()?.success != true) {
                    Log.e("MyProfileFragment", "❌ API error: ${response.code()} ${response.body()?.error}")
                    userApiCall = null
                    return
                }

                val user = response.body()?.user
                if (user == null) {
                    Log.e("MyProfileFragment", "❌ API success but user=null")
                    userApiCall = null
                    return
                }

                Log.d("MyProfileFragment", "✅ User fetched username=${user.username} email=${user.email}")
                Log.d("MyProfileFragment", "🖼 profilePicture=${user.profilePicture}")

                renderUser(user)
                cacheUser(user)

                userApiCall = null
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                if (!isAdded) return
                if (!call.isCanceled) Log.e("MyProfileFragment", "❌ Network error: ${t.message}", t)
                userApiCall = null
            }
        })
    }

    private fun renderUser(user: User) {
        val name = user.username?.takeIf { it.isNotBlank() } ?: "User"
        val email = user.email
        val initial = (user.username ?: email).firstOrNull()?.uppercase() ?: "U"

        binding.tvUsername.text = name
        binding.tvEmail.text = email

        val url = user.profilePicture?.trim()
        if (!url.isNullOrBlank()) {
            showPhoto(url)
        } else {
            showInitial(initial)
        }
    }

    private fun showPhoto(url: String) {
        binding.ivProfilePhoto.visibility = View.VISIBLE
        binding.tvAvatarInitial.visibility = View.GONE

        Glide.with(this)
            .load(url)
            .circleCrop()
            .placeholder(com.example.doggo.R.drawable.ic_profile_placeholder)
            .error(com.example.doggo.R.drawable.ic_profile_placeholder)
            .into(binding.ivProfilePhoto)
    }

    private fun showInitial(initial: String) {
        binding.ivProfilePhoto.visibility = View.GONE
        binding.tvAvatarInitial.visibility = View.VISIBLE
        binding.tvAvatarInitial.text = initial
    }

    private fun cacheUser(user: User) {
        sharedPreferences.edit().apply {
            putString("email", user.email)
            putString("username", user.username ?: "")
            putString("profile_picture", user.profilePicture ?: "")
            apply()
        }
    }

    private fun setupClickListeners() {
        // Edit Profile - only one click listener needed now
        binding.layoutEditProfile.setOnClickListener {
            if (!isAdded) return@setOnClickListener
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        // Change Password
        binding.layoutChangePassword.setOnClickListener {
            showComingSoonToast("Change Password")
        }

        // Notifications
        binding.layoutNotifications.setOnClickListener {
            showComingSoonToast("Notifications")
        }

        // App Settings
        binding.layoutPrivacyPolicy.setOnClickListener { showComingSoonToast("Privacy Policy") }
        binding.layoutTermsConditions.setOnClickListener { showComingSoonToast("Terms & Conditions") }
        binding.layoutAboutUs.setOnClickListener { showComingSoonToast("About Us") }

        // Logout
        binding.layoutLogout.setOnClickListener { showLogoutConfirmation() }
    }

    private fun showComingSoonToast(feature: String) {
        if (!isAdded) return
        Toast.makeText(requireContext(), "$feature coming soon!", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmation() {
        if (!isAdded) return

        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        if (!isAdded) return

        userApiCall?.cancel()
        sharedPreferences.edit().clear().apply()
        ProfileManager.clearProfiles()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireActivity(), IntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userApiCall?.cancel()
        userApiCall = null
        _binding = null
    }
}