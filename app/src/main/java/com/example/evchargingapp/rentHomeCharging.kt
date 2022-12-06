package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.evchargingapp.BuildConfig.GOOGLE_MAPS_API_KEY

class rentHomeCharging : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_home_charging)
        supportActionBar?.hide()
        val backButton = findViewById<Button>(R.id.backbutton);
        val submitButton = findViewById<Button>(R.id.submitButton);

        val addressInput = findViewById<EditText>(R.id.addressInput);
        val chargerTypeIn = findViewById<EditText>(R.id.chargerTypeIn);
        val timeIn = findViewById<EditText>(R.id.timeIn);
        val priceIn = findViewById<EditText>(R.id.priceIn);

        submitButton.setOnClickListener {
            addressInput.clearFocus()
            chargerTypeIn.clearFocus()
            timeIn.clearFocus()
            priceIn.clearFocus()

            addressInput.text.clear()
            chargerTypeIn.text.clear()
            timeIn.text.clear()
            priceIn.text.clear()
        }
        backButton.setOnClickListener {
            val returnIntent = Intent()
            setResult(1, returnIntent)
            finish()
        }

    }
}