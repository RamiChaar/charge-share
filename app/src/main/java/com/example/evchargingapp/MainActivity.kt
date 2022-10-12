package com.example.evchargingapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.evchargingapp.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

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
        val long = -118.5300
        val initialLocation = LatLng(lat, long)
        val zoomLevel = 12.0f;

        // Add a marker at CSUN and move the camera there
        mMap.addMarker(MarkerOptions().position(initialLocation).title("Your Location").rotation(90.0F))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, zoomLevel));

        //load stations in a two mile radius
        loadNearestStations(lat, long, 2)
    }

    private fun loadNearestStations(latitude: Double, longitude: Double, radius : Int) {

        val url = "https://developer.nrel.gov/api/alt-fuel-stations/v1/nearest.json?api_key=atG74JTz1BziqwmY0hecm8a9J14qTnbUb5SOvjPs&latitude=$latitude&longitude=$longitude&radius=$radius&limit=all"
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
        for(station in fuel_stations) {
            mMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        station.latitude,
                        station.longitude
                    )
                ).title(station.id.toString())
            )
        }
    }

}