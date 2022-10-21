package com.example.evchargingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
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
        val lat = 34.2407
        val lng = -118.5300
        val searchRadius = 10
        val initialLocation = LatLng(lat, lng)
        val zoomLevel = 12.0f

        // Add a marker at CSUN and move the camera there
        mMap.addMarker(MarkerOptions().position(initialLocation).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, zoomLevel))

        //load stations
        loadNearestStations(lat, lng, searchRadius)
        mMap.setOnMarkerClickListener(this)
    }

    private fun loadNearestStations(latitude: Double, longitude: Double, radius : Int) {

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

                //load markers on the main thread
                runOnUiThread {
                    loadMarkers(nearestStations.fuel_stations)
                }

            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }
        })
    }

    private fun loadMarkers(fuel_stations: List<Station>) {
        val defaultMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_default_marker)
        val inactiveMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_inactive_marker)
        val privateMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_private_marker)
        val levelOneMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_level1_marker)
        val levelTwoMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_level2_marker)
        val levelThreeMarker = bitmapDescriptorFromVector(applicationContext, R.drawable.ic_resource_level3_marker)

        var marker : Marker?
        for(station in fuel_stations) {
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
        return true
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