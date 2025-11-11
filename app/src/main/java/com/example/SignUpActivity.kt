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
import com.example.doggo.network.SignUpRequest
import com.example.doggo.network.SignInRequest
import com.example.doggo.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()

            val signUpRequest = SignUpRequest(email, password)

            RetrofitClient.instance.signUp(signUpRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {
                            Log.d("SignUp", "✅ Account created! User ID: ${apiResponse.userId}")
                            Toast.makeText(
                                this@SignUpActivity,
                                "Account created! Logging you in...",
                                Toast.LENGTH_SHORT
                            ).show()

                            // ✅ AUTO SIGN IN after successful sign up
                            autoSignIn(email, password)

                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                apiResponse?.error ?: "Sign up failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Sign up failed: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("SignUp", "❌ Network error: ${t.message}")
                    Toast.makeText(
                        this@SignUpActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    // ✅ Auto sign in after sign up
    private fun autoSignIn(email: String, password: String) {
        Log.d("SignUp", "🔄 Auto signing in...")

        val signInRequest = SignInRequest(email, password)

        RetrofitClient.instance.signIn(signInRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {

                        // ✅ SAVE TOKEN - Match SignInActivity keys exactly
                        val sharedPref = getSharedPreferences("doggo_pref", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("user_token", apiResponse.token)
                            putInt("user_id", apiResponse.userId ?: 0)
                            putString("user_uid", apiResponse.uid)
                            apply()
                        }

                        Log.d("SignUp", "✅ Token saved: ${apiResponse.token}")
                        Log.d("SignUp", "✅ User ID: ${apiResponse.userId}")

                        // Verify token saved
                        val savedToken = sharedPref.getString("user_token", null)
                        Log.d("SignUp", "🔍 Verify token: ${if (savedToken != null) "EXISTS" else "NULL"}")

                        Toast.makeText(
                            this@SignUpActivity,
                            "Welcome! Let's set up your profile",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to Home with clear back stack
                        val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    } else {
                        Log.e("SignUp", "❌ Auto sign in failed: ${apiResponse?.error}")
                        Toast.makeText(
                            this@SignUpActivity,
                            "Account created but auto-login failed. Please sign in manually.",
                            Toast.LENGTH_LONG
                        ).show()
                        // Go to sign in page
                        startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                        finish()
                    }
                } else {
                    Log.e("SignUp", "❌ Auto sign in HTTP error: ${response.code()}")
                    Toast.makeText(
                        this@SignUpActivity,
                        "Account created! Please sign in.",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                    finish()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("SignUp", "❌ Auto sign in network error: ${t.message}")
                Toast.makeText(
                    this@SignUpActivity,
                    "Account created! Please sign in.",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                finish()
            }
        })
    }
}