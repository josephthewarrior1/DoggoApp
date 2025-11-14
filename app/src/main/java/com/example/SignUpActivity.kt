package com.example.doggo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.network.RetrofitClient
import com.example.doggo.network.SignUpRequest
import com.example.doggo.network.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etUsername = findViewById<EditText>(R.id.etUsername) // ← TAMBAH INI
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim() // ← TAMBAH INI
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validasi semua field
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi username (optional)
            if (username.length < 3) {
                Toast.makeText(this, "Username should be at least 3 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()

            // TAMBAH username di SignUpRequest
            val signUpRequest = SignUpRequest(email, password, username)

            RetrofitClient.instance.signUp(signUpRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {

                            Log.d("SignUp", "✅ Account created successfully")

                            Toast.makeText(
                                this@SignUpActivity,
                                "Account created! Please sign in",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Redirect ke SignIn activity
                            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                            intent.putExtra("email", email) // Optional: biar email nya udah keisi
                            startActivity(intent)
                            finish()

                        } else {
                            val errorMsg = apiResponse?.error ?: "Sign up failed"
                            Log.e("SignUp", "❌ API Error: $errorMsg")
                            Toast.makeText(this@SignUpActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("SignUp", "❌ HTTP Error: ${response.code()} - ${response.message()}")
                        Toast.makeText(
                            this@SignUpActivity,
                            "Sign up failed: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.e("SignUp", "❌ Network Error: ${t.message}", t)
                    Toast.makeText(
                        this@SignUpActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}