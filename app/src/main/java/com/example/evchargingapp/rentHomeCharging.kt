package com.example.evchargingapp

import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class rentHomeCharging : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_home_charging)
        supportActionBar?.hide()
        val backButton = findViewById<Button>(R.id.backbutton);
        val addButton = findViewById<Button>(R.id.addButton);

        backButton.setOnClickListener {
            val returnIntent = Intent()
            setResult(1, returnIntent)
            finish()
        }
        addButton.setOnClickListener {
            val intent = Intent(this, AddCustomStation::class.java)
            resultLauncher.launch(intent)
        }
    }

    private fun refresh() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser
        val customList = findViewById<LinearLayout>(R.id.customList)
        customList.removeAllViews()

        if(currentUser != null) {
            val collectionRef = db.collection("CustomStations")
            val query = collectionRef.whereEqualTo("UID", currentUser.uid)
            query.get().addOnSuccessListener { querySnapshot ->
                var i = 0;
                for (document in querySnapshot) {
                    val inflater = LayoutInflater.from(this)
                    val childLayout = inflater.inflate(R.layout.custom_station, null)
                    val address = childLayout.findViewById<TextView>(R.id.address)
                    val charger = childLayout.findViewById<TextView>(R.id.charger)
                    val level = childLayout.findViewById<TextView>(R.id.level)
                    val rate = childLayout.findViewById<TextView>(R.id.rate)
                    val trashButton = childLayout.findViewById<ImageButton>(R.id.trashButton)
                    childLayout.id = i++

                    address.text = document.data["Address"].toString()
                    charger.text = "Charger Type: " + document.data["Charger"].toString()
                    level.text = "Level: " + document.data["Level"].toString()
                    rate.text = "Rate: $" + document.data["Rate"].toString() + "/hr"

                    customList.addView(childLayout)
                    Log.d("debug", childLayout.toString())

                    trashButton.setOnClickListener {
                        val docRef = collectionRef.document(document.id)
                        docRef.delete()
                        refresh()
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("debug", "Error getting documents", e)
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    override fun onResume() {
        super.onResume()
        Log.d("debug", "FavoriteActivity: " + "onResume")
        refresh()
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == 1) {
            val data: Intent? = result.data
        }
    }

}