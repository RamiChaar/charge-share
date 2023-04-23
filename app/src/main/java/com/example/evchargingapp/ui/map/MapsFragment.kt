package com.example.evchargingapp.ui.map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.evchargingapp.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.GsonBuilder
import com.google.maps.android.clustering.ClusterManager
import okhttp3.*
import java.io.IOException
import kotlin.math.min
import kotlin.math.pow


class MapsFragment : Fragment(){

    private lateinit var clusterManager: ClusterManager<StationClusterItem>
    private lateinit var clusterItemClickListener: ClusterManager.OnClusterItemClickListener<StationClusterItem>

    private lateinit var googleMap: GoogleMap
    private var mapReady = false
    private var defaultLat = 34.2407
    private var defaultLng = -118.5300
    private var setLocation = LatLng(defaultLat, defaultLng)
    private var setZoomLevel = 13.0f

    private var searchRadius = 70.0

    private val loadedStations = mutableListOf<Station>()
    private val allLevels = mutableListOf("ev_level1_evse_num", "ev_level2_evse_num", "ev_dc_fast_num")
    private val allConnectors = mutableListOf("J1772", "J1772COMBO", "TESLA", "CHADEMO", "NEMA1450", "NEMA515", "NEMA520")
    private var levels = mutableListOf("ev_level1_evse_num", "ev_level2_evse_num", "ev_dc_fast_num")
    private var connectors = mutableListOf("J1772", "J1772COMBO", "TESLA", "CHADEMO", "NEMA1450", "NEMA515", "NEMA520")
    private var showPrivate = true
    private var showInactive = true

    private val callback = OnMapReadyCallback { googleMap ->
        Log.d("debug", "MapsFragment: onMapReadyCallback")
        this.googleMap = googleMap
        mapReady = true

        googleMap.setInfoWindowAdapter(context?.let { CustomInfoWindow(it) })

        val style = context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_dark_theme) }
        if (context?.resources?.configuration?.uiMode == 33) {
            googleMap.setMapStyle(style)
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(setLocation, setZoomLevel))
        loadNearestStations(setLocation, searchRadius)
        loadNearestCustomStations()
        //googleMap.setOnMarkerClickListener(this)

        googleMap.setOnCameraIdleListener {
            val center = googleMap.cameraPosition.target
            val zoom = googleMap.cameraPosition.zoom
            searchRadius = min(16.0, 2.0.pow((14.5 - zoom)))
            loadNearestStations(center, searchRadius)
            loadNearestCustomStations()

            Log.d("debug", "camera moved to: " + center.toString())
            Log.d("debug", "search radius: " +  searchRadius.toString())
        }

        /*googleMap.setOnInfoWindowClickListener { marker ->
            val id = marker.tag
            Log.d("debug", "marker $id" +  " info Window Clicked")
            val intent = Intent(context, StationInfoActivity::class.java)
            intent.putExtra("id", id.toString())
            Log.d("debug", "launching info for " +  id.toString())
        }*/

        this.googleMap = googleMap
        clusterManager = ClusterManager(context, googleMap)
        val clusterRenderer = ClusterRenderer(requireContext(), googleMap, clusterManager)
        clusterManager.renderer = clusterRenderer
        clusterItemClickListener = object : ClusterManager.OnClusterItemClickListener<StationClusterItem> {
            override fun onClusterItemClick(item: StationClusterItem): Boolean {
                val id = item.getTag()
                Log.d("debug", "marker $id" +  " info Window Clicked")
                val intent = Intent(context, StationInfoActivity::class.java)
                intent.putExtra("id", id.toString())
                Log.d("debug", "launching info for " +  id.toString())
                return false
            }
        }

        mapReady = true

        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
            // Your existing code for OnCameraIdleListener
        }

        googleMap.setOnMarkerClickListener(clusterManager)
        clusterManager.setOnClusterItemClickListener(clusterItemClickListener)

    }

    override fun onResume() {
        super.onResume()
        Log.d("debug", "MapsFragment: " + "onResume")
    }

    private fun loadNearestStations(location: LatLng, radius: Double) {
        val latitude = location.latitude
        val longitude = location.longitude
        val url = "https://developer.nrel.gov/api/alt-fuel-stations/v1/nearest.json?api_key=atG74JTz1BziqwmY0hecm8a9J14qTnbUb5SOvjPs&fuel_type=ELEC&latitude=$latitude&longitude=$longitude&radius=$radius&limit=all"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        Log.d("debug", "loading: " +  "response requested")
        //make API call to client
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {

                //retrieve json body and use gson to create nearest station object
                val body = response.body?.string()
                val gson = GsonBuilder().create()
                val newNearestStations = gson.fromJson(body, NearestStations::class.java)

                //load markers on the main thread
                activity?.runOnUiThread {
                    Log.d("debug", "loading: " +  "response fetched")
                    loadMarkers(newNearestStations)
                }

            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute request")
            }
        })
    }

    private fun loadMarkers(newNearestStations: NearestStations) {
        val defaultMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_default_marker) }
        val inactiveMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_inactive_marker) }
        val privateMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_private_marker) }
        val levelOneMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_level1_marker) }
        val levelTwoMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_level2_marker) }
        val levelThreeMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_level3_marker) }

        val currIds = mutableListOf<Int>()
        loadedStations.forEach { station ->
            currIds.add(station.id)
        }

        for(station in newNearestStations.fuel_stations) {
            if(currIds.contains(station.id)){
                continue
            }
            loadedStations.add(station)

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
                if(levels.contains(level)) {
                    hasOneLevel = true
                }
            }

            if(station.ev_connector_types == null) {
                hasOneConnector = true
            } else {
                for (connector in station.ev_connector_types) {
                    if (connectors.contains(connector)) {
                        hasOneConnector = true
                    }
                }
            }

            if(!hasOneLevel || !hasOneConnector){
                continue
            }

            if(!showPrivate && station.access_code == "private"){
                continue
            }

            if(!showInactive && station.status_code != "E"){
                continue
            }

            var snippetString = ""

            val markerIcon = when {
                station.status_code != "E" -> {
                    snippetString += "Status: Inactive"
                    inactiveMarker
                }
                station.access_code == "private" -> {
                    snippetString += "Status: Active\nAccess: Private\nLevel: Fast (Level3)"
                    privateMarker
                }
                station.ev_dc_fast_num > 0 -> {
                    snippetString += "Status: Active\nAccess: Public\nLevel: Fast (Level3)"
                    levelThreeMarker
                }
                station.ev_level2_evse_num > 0 -> {
                    snippetString += "Status: Active\nAccess: Public\nLevel: Level 2"
                    levelTwoMarker
                }
                station.ev_level1_evse_num > 0 -> {
                    snippetString += "Status: Active\nAccess: Public\nLevel: Level 1"
                    levelOneMarker
                }
                else -> {
                    snippetString += "Details Unknown"
                    defaultMarker
                }
            }

            val stationClusterItem = StationClusterItem(
                LatLng(station.latitude, station.longitude),
                station.station_name,
                snippetString,
                station.id,
                markerIcon
            )

            clusterManager.addItem(stationClusterItem)
        }
        Log.d("debug", "loading: " + "markers loaded")
    }

    private fun loadNearestCustomStations() {
        val visibleRegion = googleMap.projection.visibleRegion

        val northeast = visibleRegion.latLngBounds.northeast
        val southwest = visibleRegion.latLngBounds.southwest

        val northeastLat = northeast.latitude
        val northeastLng = northeast.longitude
        val southwestLat = southwest.latitude
        val southwestLng = southwest.longitude

        val db = FirebaseFirestore.getInstance()

        db.collection("CustomStations")
            .whereGreaterThan("Latitude", southwestLat)
            .whereLessThan("Latitude", northeastLat)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val customMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_custom_marker) }
                for (document in querySnapshot.documents) {
                    var lng = document.data?.get("Longitude") as Double
                    var lat = document.data?.get("Latitude") as Double

                    if(lng < southwestLng || lng > northeastLng){
                        continue;
                    }

                    var address = document.data?.get("Address").toString()
                    var charger = document.data?.get("Charger").toString()
                    var level = document.data?.get("Level").toString()
                    var rate = document.data?.get("Rate").toString()
                    var owner = document.data?.get("Owner").toString()
                    var id : Long = document.data?.get("id") as Long
                    var snippetString = ""
                    snippetString += "Owner: $owner"
                    snippetString += "\nCharger Type: $charger"
                    snippetString += "\nLevel: $level"
                    snippetString += "\nRate: $$rate"

                    // Create a StationClusterItem for custom stations
                    val customStationClusterItem = StationClusterItem(
                        LatLng(lat, lng),
                        address.substringBefore(","),
                        snippetString,
                        id.toInt(),
                        customMarker
                    )

                    // Add customStationClusterItem to the ClusterManager
                    clusterManager.addItem(customStationClusterItem)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during the query
            }
    }


    /*private fun loadNearestCustomStations() {
        val visibleRegion = googleMap.projection.visibleRegion

        val northeast = visibleRegion.latLngBounds.northeast
        val southwest = visibleRegion.latLngBounds.southwest

        Log.d("debug", "northeast: " + northeast.toString())
        Log.d("debug", "southwest: " + southwest.toString())

        val northeastLat = northeast.latitude
        val northeastLng = northeast.longitude
        val southwestLat = southwest.latitude
        val southwestLng = southwest.longitude

        val db = FirebaseFirestore.getInstance()

        db.collection("CustomStations")
            .whereGreaterThan("Latitude", southwestLat)
            .whereLessThan("Latitude", northeastLat)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val customMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_custom_marker) }
                for (document in querySnapshot.documents) {
                    Log.d("debug", document.data.toString())
                    var lng = document.data?.get("Longitude") as Double
                    var lat = document.data?.get("Latitude") as Double

                    if(lng < southwestLng || lng > northeastLng){
                        continue;
                    }

                    var address = document.data?.get("Address").toString()
                    var charger = document.data?.get("Charger").toString()
                    var level = document.data?.get("Level").toString()
                    var rate = document.data?.get("Rate").toString()
                    var owner = document.data?.get("Owner").toString()
                    var id : Long = document.data?.get("id") as Long
                    var snippetString = ""
                    snippetString += "Owner: $owner"
                    snippetString += "\nCharger Type: $charger"
                    snippetString += "\nLevel: $level"
                    snippetString += "\nRate: $$rate"

                    var marker : Marker? = googleMap.addMarker(MarkerOptions().position(LatLng(lat, lng)).title(address.substringBefore(",")))
                    marker?.tag = id
                    marker?.setIcon(customMarker)
                    marker?.snippet =snippetString
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during the query
            }
    }*/

    /*private fun loadNearestCustomStations() {
        val visibleRegion = googleMap.projection.visibleRegion

        val northeast = visibleRegion.latLngBounds.northeast
        val southwest = visibleRegion.latLngBounds.southwest

        Log.d("debug", "northeast: " + northeast.toString())
        Log.d("debug", "southwest: " + southwest.toString())

        val northeastLat = northeast.latitude
        val northeastLng = northeast.longitude
        val southwestLat = southwest.latitude
        val southwestLng = southwest.longitude

        val db = FirebaseFirestore.getInstance()

        db.collection("CustomStations")
            .whereGreaterThan("Latitude", southwestLat)
            .whereLessThan("Latitude", northeastLat)
            .get()
            .addOnSuccessListener { querySnapshot ->
                loadCustomMarkers(querySnapshot)
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during the query
            }
    }


    private fun loadCustomMarkers(querySnapshot: QuerySnapshot) {
        val customMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_resource_custom_marker) }

        for (document in querySnapshot.documents) {
            Log.d("debug", document.data.toString())
            val lng = document.data?.get("Longitude") as Double
            val lat = document.data?.get("Latitude") as Double

            val visibleRegion = googleMap.projection.visibleRegion
            val southwestLng = visibleRegion.latLngBounds.southwest.longitude
            val northeastLng = visibleRegion.latLngBounds.northeast.longitude

            if (lng < southwestLng || lng > northeastLng) {
                continue
            }

            val address = document.data?.get("Address").toString()
            val charger = document.data?.get("Charger").toString()
            val level = document.data?.get("Level").toString()
            val rate = document.data?.get("Rate").toString()
            val owner = document.data?.get("Owner").toString()
            val id: Long = document.data?.get("id") as Long
            var snippetString = ""
            snippetString += "Owner: $owner"
            snippetString += "\nCharger Type: $charger"
            snippetString += "\nLevel: $level"
            snippetString += "\nRate: $$rate"

            val stationClusterItem = StationClusterItem(
                LatLng(lat, lng),
                address,
                snippetString,
                id.toInt(),
                customMarker
            )

            clusterManager.addItem(stationClusterItem)
        }
    }*/


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("debug", "MapsFragment: " + "onCreateView")
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("debug", "MapsFragment: " + "onViewCreated")
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    private fun bitmapDescriptorFromVector(context : Context, vectorResId : Int): BitmapDescriptor {
        val vectorDrawable : Drawable = ContextCompat.getDrawable(context, vectorResId)!!
        val width = vectorDrawable.intrinsicWidth
        val height = vectorDrawable.intrinsicHeight
        val bitmap : Bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, width, height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}