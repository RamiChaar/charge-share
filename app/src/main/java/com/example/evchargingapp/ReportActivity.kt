package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        val intent = intent
        val stationId = intent.getStringExtra("id")

        val reportButton = findViewById<Button>(R.id.ReportSubmitButton)
        reportButton.setOnClickListener {

            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()
            val currentUser = auth.currentUser

            if(currentUser == null) {
                val returnIntent = Intent()
                setResult(1, returnIntent)
                finish()
                return@setOnClickListener
            }

            val collectionRef = db.collection("StationReports")
            val reportInputTxt = findViewById<EditText>(R.id.ReportInputTxt)
            val reportString : String = reportInputTxt.text.toString()

            if (reportString == "") {
                return@setOnClickListener
            }

            val newReport = hashMapOf(
                "UID" to currentUser.uid,
                "Report" to reportString,
                "stationId" to stationId,
            )
            collectionRef.add(newReport)

            val returnIntent = Intent()
            setResult(1, returnIntent)
            finish()
        }

        val viewActionBar = layoutInflater.inflate(R.layout.station_info_bar, null);
        val toolBarLabel = viewActionBar.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.toolBarTitle)
        toolBarLabel.text = "Report Station"
        supportActionBar?.customView = viewActionBar;
        supportActionBar?.setDisplayShowCustomEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setHomeButtonEnabled(true);
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val returnIntent = Intent()
        setResult(1, returnIntent)
        finish()
        return super.onOptionsItemSelected(menuItem)
    }

}