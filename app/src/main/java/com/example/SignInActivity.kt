package com.example.doggo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.Home.HomeActivity
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.SignInRequest
import com.example.doggo.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnToSignUp = findViewById<Button>(R.id.btnToSignUp)

        // Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()

            val signInRequest = SignInRequest(email, password)

            RetrofitClient.instance.signIn(signInRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {

                            // ✅ SIMPAN SEMUA DATA USER + USERNAME
                            val sharedPref = getSharedPreferences("doggo_pref", MODE_PRIVATE)
                            sharedPref.edit().apply {
                                putString("user_token", apiResponse.token)
                                putString("user_id", apiResponse.userId?.toString() ?: "0") // ← FIX: Convert to String
                                putString("user_uid", apiResponse.uid)
                                putString("user_db_id", apiResponse.userDbId?.toString() ?: "0") // ← Juga ini
                                putString("username", apiResponse.username ?: "User")
                                apply()
                            }

                            Log.d("SignIn", "✅ Login successful!")
                            Log.d("SignIn", "   - Username: ${apiResponse.username}")
                            Log.d("SignIn", "   - User ID: ${apiResponse.userId}")
                            Log.d("SignIn", "   - Token: ${apiResponse.token?.take(20)}...")

                            // Verifikasi data tersimpan
                            val savedUsername = sharedPref.getString("username", null)
                            val savedUserId = sharedPref.getString("user_id", null)
                            Log.d("SignIn", "🔍 Verified - Username: $savedUsername, UserId: $savedUserId")

                            Toast.makeText(
                                this@SignInActivity,
                                "Welcome back, ${apiResponse.username}! 🐶",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Redirect ke HomeActivity
                            startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
                            finish()

                        } else {
                            val errorMsg = apiResponse?.error ?: "Login failed"
                            Log.e("SignIn", "❌ API Error: $errorMsg")
                            Toast.makeText(this@SignInActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("SignIn", "❌ HTTP Error: ${response.code()} - ${response.message()}")
                        Toast.makeText(
                            this@SignInActivity,
                            "Login failed: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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

        // Pindah ke Sign Up
        btnToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}