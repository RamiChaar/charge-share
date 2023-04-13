package com.example.evchargingapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import kotlin.properties.Delegates
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class StationInfoActivity : AppCompatActivity() {

    private var longitude by Delegates.notNull<Double>()
    private var latitude by Delegates.notNull<Double>()
    private var name by Delegates.notNull<String>()
    private var access by Delegates.notNull<String>()
    private var status by Delegates.notNull<String>()
    private var numLevelOne by Delegates.notNull<Int>()
    private var numLevelTwo by Delegates.notNull<Int>()
    private var numLevelThree by Delegates.notNull<Int>()
    private var loaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_info)
        val favoriteButton = findViewById<ImageButton>(R.id.favoriteButton)
        val navigateButton = findViewById<ImageButton>(R.id.navigateButton)

        val intent = intent
        val id = intent.getStringExtra("id")

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        var inFavorites = false

        if (currentUser != null && id != null) {
            val collectionRef = db.collection("FavoriteStations")
            val query = collectionRef.whereEqualTo("UID", currentUser.uid).whereEqualTo("id", id)

            query.get().addOnSuccessListener { querySnapshot ->
                // Query snapshot contains all documents in the "users" collection
                for (document in querySnapshot) {
                    if(document.data["id"] == id &&  document.data["UID"] == currentUser.uid) {
                        inFavorites = true
                        favoriteButton.setBackgroundResource(R.drawable.favorite_button_selected)
                    }
                }
            }.addOnFailureListener { e ->
                // Handle errors
                Log.e("debug", "Error getting documents", e)
            }
            Log.d("debug", "User UID: ${currentUser.uid}")
        } else {
            Log.d("debug", "No user is currently signed in.")
        }

        navigateButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
            )
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }

        favoriteButton.setOnClickListener {
            if(currentUser == null || id == null || !loaded) {
                return@setOnClickListener
            }
            val collectionRef = db.collection("FavoriteStations")
            val query = collectionRef.whereEqualTo("UID", currentUser.uid).whereEqualTo("id", id)
            if(!inFavorites) {
                var levelString = ""
                if(numLevelThree > 0) {
                    levelString = "Fast (Level3)"
                } else if(numLevelTwo > 0) {
                    levelString = "Level 2"
                } else if(numLevelOne > 0) {
                    levelString = "Level 1"
                }
                val newFavorite = hashMapOf(
                    "UID" to currentUser.uid,
                    "id" to id,
                    "name" to name,
                    "access" to access,
                    "status" to status,
                    "level" to levelString
                )
                collectionRef.add(newFavorite)
                favoriteButton.setBackgroundResource(R.drawable.favorite_button_selected)
                inFavorites = true
            } else {
                query.get().addOnSuccessListener { querySnapshot ->
                    // Delete documents that match the query
                    for (documentSnapshot in querySnapshot.documents) {
                        // Delete document
                        documentSnapshot.reference.delete()
                    }
                }
                favoriteButton.setBackgroundResource(R.drawable.favorite_button_unselected)
                inFavorites = false
            }
        }

        val reportButton = findViewById<ImageButton>(R.id.reportButton)
        reportButton.setOnClickListener {
            //to implement - report button
        }

        if (id != null) {
            getStation(id)
        }
    }

    private fun getStation(id : String) {
        var url = "https://developer.nrel.gov/api/alt-fuel-stations/v1/$id.json?api_key=atG74JTz1BziqwmY0hecm8a9J14qTnbUb5SOvjPs"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        Log.d("debug", "loading: " + "station response requested")
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
                    loaded = true
                }

            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }
        })
    }

    fun loadData(station: SingleStation) {
        val accessField = findViewById<TextView>(R.id.access)
        val statusField = findViewById<TextView>(R.id.status)
        val connectorsField = findViewById<TextView>(R.id.connectors)
        val addressField = findViewById<TextView>(R.id.address)
        val phoneField = findViewById<TextView>(R.id.phone)
        val timeField = findViewById<TextView>(R.id.timeInfo)
        val pricingField = findViewById<TextView>(R.id.pricingInfo)
        val directionField = findViewById<TextView>(R.id.directionInfo)
        val networkField = findViewById<TextView>(R.id.network)
        val networkWebField = findViewById<TextView>(R.id.networkWeb)


        name = station.station_name
        access = station.access_code
        status = station.status_code
        var connectors = ""
        var connectorsTypes = station.ev_connector_types
        numLevelOne = station.ev_level1_evse_num
        numLevelTwo = station.ev_level2_evse_num
        numLevelThree = station.ev_dc_fast_num
        var address = station.street_address + ", " + station.city + ", " + station.state
        var phoneNumber = station.station_phone
        var timeDetails = station.access_days_time
        var pricingInfo = station.ev_pricing
        var directionInfo = station.intersection_directions
        var network = station.ev_network
        var networkWeb = station.ev_network_web
        longitude = station.longitude
        latitude = station.latitude

        if (status == "E") {
            status = "Functional"
        } else if (status == "P") {
            status = "Planned"
        } else {
            status = "Temporarily Unavailable"
        }

        if(phoneNumber == null){
            phoneNumber = "None"
        }
        if(pricingInfo == null){
            pricingInfo = "Unknown"
        }
        if(directionInfo == null){
            directionInfo = "None"
        }
        if(networkWeb == null){
            networkWeb = "None"
        }

        var numTypes = 0
        if(numLevelOne > 0) {
            numTypes += 1
            connectors += "Level 1 x $numLevelOne"
        }
        if(numLevelTwo > 0) {
            if(numTypes > 0){
                connectors += "\n"
            }
            numTypes += 1
            connectors += "Level 2 x $numLevelTwo"
        }
        if(numLevelThree > 0) {
            if(numTypes > 0){
                connectors += "\n"
            }
            connectors += "Fast (Level 3) x $numLevelThree"
        }

        accessField.text = access
        statusField.text = status
        connectorsField.text = connectors
        addressField.text = address
        phoneField.text = phoneNumber
        timeField.text = timeDetails
        pricingField.text = pricingInfo
        directionField.text = directionInfo
        networkField.text = network
        networkWebField.text = networkWeb

        val viewActionBar = layoutInflater.inflate(R.layout.station_info_bar, null);
        val toolBarLabel = viewActionBar.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.toolBarTitle)
        toolBarLabel.text = name
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