package com.example.doggo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.doggo.Home.HomeActivity
import com.example.doggo.network.ApiResponse
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.SignInRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "doggo_pref"
        private const val KEY_TOKEN = "user_token"
        private const val KEY_USER_DB_ID = "user_db_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_UID = "user_uid"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_KEEP_LOGGED_IN = "keep_logged_in"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnToSignUp = findViewById<Button>(R.id.btnToSignUp)

        // ✅ must exist in XML
        val switchKeep = findViewById<SwitchCompat>(R.id.switchKeepLoggedIn)

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)

        // Restore last switch state
        switchKeep.isChecked = prefs.getBoolean(KEY_KEEP_LOGGED_IN, false)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

            val request = SignInRequest(email, password)

            RetrofitClient.instance.signIn(request).enqueue(object : Callback<ApiResponse> {

                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (!response.isSuccessful) {
                        Log.e("SignIn", "❌ HTTP Error: ${response.code()} - ${response.message()}")
                        Toast.makeText(
                            this@SignInActivity,
                            "Login failed: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val api = response.body()
                    if (api?.success != true) {
                        val msg = api?.error ?: "Login failed"
                        Log.e("SignIn", "❌ API Error: $msg")
                        Toast.makeText(this@SignInActivity, msg, Toast.LENGTH_SHORT).show()
                        return
                    }

                    val keepLoggedIn = switchKeep.isChecked

                    // ✅ Save user data
                    prefs.edit().apply {
                        putString(KEY_TOKEN, api.token)
                        putInt(KEY_USER_DB_ID, api.userDbId ?: 0)
                        putString(KEY_USER_ID, api.userId?.toString() ?: "0")
                        putString(KEY_UID, api.uid)
                        putString(KEY_USERNAME, api.username ?: "User")
                        putString(KEY_EMAIL, email)

                        // ✅ Save keep logged in flag
                        putBoolean(KEY_KEEP_LOGGED_IN, keepLoggedIn)

                        apply()
                    }

                    Log.d("SignIn", "✅ Login successful!")
                    Log.d("SignIn", "   - keep_logged_in: $keepLoggedIn")
                    Log.d("SignIn", "   - Username: ${api.username}")
                    Log.d("SignIn", "   - UserDbId: ${api.userDbId}")
                    Log.d("SignIn", "   - Token: ${api.token?.take(20)}...")

                    Toast.makeText(
                        this@SignInActivity,
                        "Welcome back, ${api.username}! 🐶",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
                    finish()
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("SignIn", "❌ Network error: ${t.message}", t)
                    Toast.makeText(
                        this@SignInActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        btnToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}