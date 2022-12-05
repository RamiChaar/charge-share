package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class rentHomeCharging : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_home_charging)

        val backButton = findViewById<Button>(R.id.backbutton);

        backButton.setOnClickListener {
            val returnIntent = Intent()
            setResult(1, returnIntent)
            finish()
        }

    }
}