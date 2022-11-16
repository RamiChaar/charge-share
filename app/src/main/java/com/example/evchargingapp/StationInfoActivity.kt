package com.example.evchargingapp

import SingleStation
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException

class StationInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_info)

        val backButton = findViewById<Button>(R.id.backButton)

        val intent = intent
        val id = intent.getStringExtra("id")

        if (id != null) {
            getStation(id)
        }

        backButton.setOnClickListener{
            val returnIntent = Intent()
            setResult(1, returnIntent)
            finish()
        }
    }

    fun getStation(id : String) {
        var url = "https://developer.nrel.gov/api/alt-fuel-stations/v1/$id.json?api_key=atG74JTz1BziqwmY0hecm8a9J14qTnbUb5SOvjPs"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        Log.i("loading", "response requested")
        //make API call to client
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {

                //retrieve json body and use gson to create nearest station object

                val tempBody = response.body?.string()
                val n = tempBody?.length?.minus(1)
                val body = n?.let { tempBody?.substring(20, it) }
                val gson = GsonBuilder().create()
                val station = gson.fromJson(body, SingleStation::class.java)

                //load markers on the main thread
                runOnUiThread {
                    Log.i("loading", "response fetched")
                    loadData(station)
                }

            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }
        })
    }

    fun loadData(station : SingleStation) {
        val nameField = findViewById<TextView>(R.id.station_name)
        val accessField= findViewById<TextView>(R.id.access)
        val statusField = findViewById<TextView>(R.id.status)
        val connectorsField = findViewById<TextView>(R.id.connectors)
        val addressField = findViewById<TextView>(R.id.address)
        val phoneField = findViewById<TextView>(R.id.phone)
        val timeField = findViewById<TextView>(R.id.time_info)
        val pricingField = findViewById<TextView>(R.id.pricing_info)
        val directionField = findViewById<TextView>(R.id.direction_info)
        val networkField = findViewById<TextView>(R.id.network)

        var name = station.station_name
        var access = station.access_code
        var status = station.status_code
        var connectors = ""
        var connectorsTypes = station.ev_connector_types
        var numLevelOne = station.ev_level1_evse_num
        var numLevelTwo = station.ev_level2_evse_num
        var numLevelThree = station.ev_dc_fast_num
        var address = station.street_address + " " + station.city + ", " + station.state
        var phoneNumber = station.station_phone
        var timeDetails = station.access_days_time
        var pricingInfo = station.ev_pricing
        var directionInfo = station.intersection_directions
        var network = station.ev_network

        if(status == "E"){
            status = "Available"
        } else if(status == "P"){
            status = "Planned"
        } else {
            status = "Temporarily Unavailable"
        }

        for (i in 1..numLevelOne) {
            connectors += "\n                     Level 1"
        }
        for (i in 1..numLevelTwo) {
            connectors += "\n                     Level 2"
        }
        for (i in 1..numLevelThree) {
            connectors += "\n                     Fast (Level 3)"
        }

        nameField.text = "Station Name: " + name
        accessField.text = "Access: " + access
        statusField.text = "Status: " + status
        connectorsField.text = "Connectors: " + connectors
        addressField.text = "Address: " + address
        phoneField.text = "Phone: " + phoneNumber
        timeField.text = "Time Details: " + timeDetails
        pricingField.text = "Pricing Details: " + pricingInfo
        directionField.text = "Direction Information: " + directionInfo
        networkField.text = "Network: " + network
    }

}