package com.example.evchargingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val alreadyHaveAccountBtn = findViewById<TextView>(com.example.evchargingapp.R.id.alreadyHaveAccount)

        alreadyHaveAccountBtn.setOnClickListener {

        }
    }

}