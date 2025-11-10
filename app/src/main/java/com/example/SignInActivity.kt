package com.example.doggo

import android.content.Intent
import android.os.Bundle
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

            // ✅ PAKAI BACKEND NODE.JS, BUKAN LANGSUNG FIREBASE
            val signInRequest = SignInRequest(email, password)

            RetrofitClient.instance.signIn(signInRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {
                            Toast.makeText(this@SignInActivity, "Welcome back! User #${apiResponse.userId}", Toast.LENGTH_SHORT).show()

                            // Simpan data user jika perlu
                            // val sharedPref = getSharedPreferences("doggo_pref", MODE_PRIVATE)
                            // sharedPref.edit().putInt("user_id", apiResponse.userId ?: 0).apply()
                            // sharedPref.edit().putString("user_token", apiResponse.token).apply()

                            startActivity(Intent(this@SignInActivity, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@SignInActivity, apiResponse?.error ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignInActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@SignInActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Pindah ke Sign Up
        btnToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}