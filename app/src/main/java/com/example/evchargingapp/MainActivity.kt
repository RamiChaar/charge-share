package com.example.evchargingapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.evchargingapp.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private val allLevels = mutableListOf("ev_level1_evse_num", "ev_level2_evse_num", "ev_dc_fast_num")
    private val allConnectors = mutableListOf("NEMA1450", "NEMA515", "NEMA520", "J1772", "J1772COMBO", "CHADEMO", "TESLA")

    private val defaultLat = 34.2407
    private val defaultLng = -118.5300
    private val defaultLocation = LatLng(defaultLat, defaultLng)
    private val zoomLevel = 12.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val searchRadius = 2

        // Add a marker at CSUN and move the camera there
        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, zoomLevel))

        //load stations
        loadNearestStations(defaultLat, defaultLng, searchRadius, allLevels, allConnectors)
        mMap.setOnMarkerClickListener(this)
    }

    private fun loadNearestStations(latitude: Double, longitude: Double, radius: Int, levels: MutableList<String>, connectors: MutableList<String>) {
        val url = "https://developer.nrel.gov/api/alt-fuel-stations/v1/nearest.json?api_key=atG74JTz1BziqwmY0hecm8a9J14qTnbUb5SOvjPs&fuel_type=ELEC&latitude=$latitude&longitude=$longitude&radius=$radius&limit=all"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        //make API call to client
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {

                //retrieve json body and use gson to create nearest station object
                val body = response.body?.string()
                val gson = GsonBuilder().create()
                val nearestStations = gson.fromJson(body, NearestStations::class.java)

                //add the filters to the object
                nearestStations.levelFilter = levels
                nearestStations.connectorFilter = connectors

                //load markers on the main thread
                runOnUiThread {
                    loadMarkers(nearestStations)
                }

            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }
        })
    }

    private fun loadMarkers(nearestStations: NearestStations) {
        val defaultMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_default_marker)
        val inactiveMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_inactive_marker)
        val privateMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_private_marker)
        val levelOneMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_level1_marker)
        val levelTwoMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_level2_marker)
        val levelThreeMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_level3_marker)

        var marker : Marker?
        for(station in nearestStations.fuel_stations) {
            var hasOneLevel = false
            var hasOneConnector = false
            val levelsInStation = mutableListOf("")
            if(station.ev_level1_evse_num > 0){
                levelsInStation.add("ev_level1_evse_num")
            }
            if(station.ev_level2_evse_num > 0){
                levelsInStation.add("ev_level2_evse_num")
            }
            if(station.ev_dc_fast_num > 0){
                levelsInStation.add("ev_dc_fast_num")
            }

            for(level in levelsInStation){
                if(nearestStations.levelFilter.contains(level)) {
                    hasOneLevel = true
                }
            }
            for(connector in station.ev_connector_types){
                if(nearestStations.connectorFilter.contains(connector)) {
                    hasOneConnector = true
                }
            }

            if(!hasOneLevel || !hasOneConnector){
                continue
            }

            marker = mMap.addMarker(MarkerOptions().position(LatLng(station.latitude, station.longitude)).title(station.station_name))
            marker?.tag = station.id
            marker?.setIcon(defaultMarker)
            if(station.status_code != "E") {
                marker?.setIcon(inactiveMarker)
                println("inactive")
            } else if(station.access_code == "private") {
                marker?.setIcon(privateMarker)
                println("private")
            } else if(station.ev_dc_fast_num > 0) {
                marker?.setIcon(levelThreeMarker)
                println("fast")
            } else if(station.ev_level2_evse_num > 0) {
                marker?.setIcon(levelTwoMarker)
                println("medium")
            } else if(station.ev_level1_evse_num > 0) {
                marker?.setIcon(levelOneMarker)
                println("slow")
            } else {
                marker?.setIcon(defaultMarker)
                println("none")
            }
        }
    }

    override fun onMarkerClick(marker : Marker): Boolean {
        val id = marker.tag as? Int
        println("Marker $id has been clicked on.")
        marker.showInfoWindow()
        openFilterActivity()
        return true
    }

    private fun openFilterActivity() {
        val intent = Intent(this, FilterActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun bitmapDescriptorFromVector(context : Context, vectorResId : Int): BitmapDescriptor {
        val vectorDrawable : Drawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
        val bitmap : Bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        val canvas : Canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}