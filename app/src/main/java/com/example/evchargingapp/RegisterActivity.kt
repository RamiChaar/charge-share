package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val text = findViewById<TextView>(R.id.alreadyHaveAccount)

        text.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
        }
    }
}