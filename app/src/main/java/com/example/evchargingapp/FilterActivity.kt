package com.example.evchargingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast

class FilterActivity : AppCompatActivity() {

//    private var radiusSlider = findViewById<SeekBar>(R.id.radiusBar)
//    private var level1 = findViewById<Switch>(R.id.level1)
//    private var level2 = findViewById<Switch>(R.id.level2)
//    private var level3 = findViewById<Switch>(R.id.level3)
//    private var J1772 = findViewById<Switch>(R.id.J1772)
//    private var J1772COMBO = findViewById<Switch>(R.id.J1772COMBO)
//    private var TESLA = findViewById<Switch>(R.id.TESLA)
//    private var CHADEMO = findViewById<Switch>(R.id.CHADEMO)
//    private var NEMA1450 = findViewById<Switch>(R.id.NEMA1450)
//    private var NEMA515 = findViewById<Switch>(R.id.NEMA515)
//    private var NEMA520 = findViewById<Switch>(R.id.NEMA520)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener{
            finish()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}