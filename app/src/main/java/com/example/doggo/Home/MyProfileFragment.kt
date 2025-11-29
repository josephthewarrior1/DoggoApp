    package com.example.doggo.Home

    import android.content.Context
    import android.content.Intent
    import android.content.SharedPreferences
    import android.os.Bundle
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Toast
    import androidx.appcompat.app.AlertDialog
    import androidx.fragment.app.Fragment
    import com.example.doggo.Home.profile.ProfileManager
    import com.example.doggo.IntroActivity
    import com.example.doggo.databinding.FragmentMyProfileBinding

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

        private fun loadUserInfo() {
            val username = sharedPreferences.getString("username", "User") ?: "User"
            val email = sharedPreferences.getString("email", "user@example.com") ?: "user@example.com"

            binding.tvUsername.text = username
            binding.tvEmail.text = email

            // Set initial dari username untuk avatar
            binding.tvAvatarInitial.text = username.firstOrNull()?.uppercase() ?: "U"
        }

        private fun setupClickListeners() {
            // Account Settings
            binding.layoutEditProfile.setOnClickListener {
                showComingSoonToast("Edit Profile")
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