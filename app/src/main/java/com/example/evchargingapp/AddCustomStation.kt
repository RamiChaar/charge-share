package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.location.Geocoder
import androidx.annotation.RequiresApi
import java.io.IOException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.GeoPoint
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.abs

class AddCustomStation : AppCompatActivity() {

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_custom_station)
        supportActionBar?.hide()
        val backButton = findViewById<Button>(R.id.backbutton);
        val submitButton = findViewById<Button>(R.id.submitButton);

        val addressInput = findViewById<EditText>(R.id.addressInput);
        val cityInput = findViewById<EditText>(R.id.cityInput);
        val stateInput = findViewById<Spinner>(R.id.stateInput);
        val chargerTypeIn = findViewById<Spinner>(R.id.chargerTypeIn);
        val chargerLevel = findViewById<Spinner>(R.id.chargerLevel);
        val priceIn = findViewById<EditText>(R.id.priceIn);

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if(currentUser == null) {
            submitButton.isEnabled = false
            submitButton.isClickable = false
            submitButton.alpha = 0.5f
        }
        submitButton.setOnClickListener {

            var charger = chargerTypeIn.selectedItem.toString()
            var level = chargerLevel.selectedItem.toString()
            var address = addressInput.text.toString()
            var city = cityInput.text.toString()
            var state = stateInput.selectedItem.toString()
            var rate = priceIn.text.toString()

            Log.d("debug", "charger: " + charger)
            Log.d("debug", "level: " + level)
            Log.d("debug", "address: " + address)
            Log.d("debug", "rate: " + rate)
            Log.d("debug", "city: " + city)
            Log.d("debug", "state: " + state)

            var queryAddress = "$address, $city, $state"
            var location = getAddressLatLng(queryAddress, charger, level, rate)



            priceIn.clearFocus()
            addressInput.clearFocus()
            cityInput.clearFocus()
            chargerTypeIn.setSelection(0)
            chargerLevel.setSelection(0)
            stateInput.setSelection(0)
            priceIn.text.clear()
            addressInput.text.clear()
            cityInput.text.clear()
        }
        backButton.setOnClickListener {
            val returnIntent = Intent()
            setResult(1, returnIntent)
            finish()
        }
    }

    @RequiresApi(33)
    private fun getAddressLatLng(address: String, charger: String, level: String, rate: String): Pair<Double, Double>? {
        val geocoder = Geocoder(this)
        val geocodeListener = Geocoder.GeocodeListener { addresses ->
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            val db = FirebaseFirestore.getInstance()
            val collectionRef = db.collection("CustomStations")

            if (addresses.isNotEmpty()) {
                val lat = addresses[0].latitude
                val lng = addresses[0].longitude

                val newCustomStation = hashMapOf(
                    "UID" to currentUser?.uid,
                    "Owner" to currentUser?.email?.substringBefore("@"),
                    "Address" to address,
                    "Charger" to charger,
                    "Level" to level,
                    "Rate" to rate,
                    "Latitude" to lat,
                    "Longitude" to lng
                )
                collectionRef.add(newCustomStation).addOnSuccessListener { documentReference ->
                    // Get the generated document ID
                    val docId = documentReference.id

                    // Update the document with the intId field
                    val updateData = hashMapOf(
                        "id" to didToUniqueInt(docId)
                    )
                    collectionRef.document(documentReference.id).update(updateData as Map<String, Any>)
                }
                val returnIntent = Intent()
                setResult(1, returnIntent)
                finish()
            }
        }
        try {
            geocoder.getFromLocationName(address, 1, geocodeListener)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun didToUniqueInt(did: String): Int {
        // Convert the UID string to bytes
        val uidBytes = did.toByteArray()

        // Compute the MD5 hash of the UID bytes
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest(uidBytes)

        // Convert the digest bytes to a BigInteger
        val bigInt = BigInteger(1, digest)

        // Get the absolute value of the BigInteger as an Int
        val uniqueInt = abs(bigInt.toInt())

        // Return the unique integer
        return uniqueInt
    }
}