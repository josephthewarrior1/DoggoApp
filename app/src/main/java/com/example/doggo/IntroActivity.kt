package com.example.doggo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val btnLetsGo = findViewById<Button>(R.id.btnLetsGo)
        btnLetsGo.setOnClickListener {
            // ⬇️ Ubah MainActivity jadi SignUpActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Supaya ga bisa balik ke intro
        }
    }
}
