package com.example.doggo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.doggo.Home.HomeActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        Handler(Looper.getMainLooper()).postDelayed({

            val prefs = getSharedPreferences("doggo_pref", MODE_PRIVATE)

            val keepLoggedIn = prefs.getBoolean("keep_logged_in", false)
            val token = prefs.getString("user_token", null)

            val nextIntent = if (keepLoggedIn && !token.isNullOrEmpty()) {
                Intent(this, HomeActivity::class.java)
            } else {
                Intent(this, SignInActivity::class.java)
            }

            startActivity(nextIntent)
            finish()

        }, 2500)
    }
}