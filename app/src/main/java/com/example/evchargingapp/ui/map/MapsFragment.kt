package com.example.evchargingapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.evchargingapp.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitFindCurrentPlace
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.GsonBuilder
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import kotlin.math.min
import kotlin.math.pow


class MapsFragment : Fragment(){

    private lateinit var clusterManager: ClusterManager<StationClusterItem>
    private lateinit var clusterItemInfoWindowClickListener: ClusterManager.OnClusterItemInfoWindowClickListener<StationClusterItem>

    private lateinit var placesClient: PlacesClient
    private lateinit var googleMap: GoogleMap
    private lateinit var refreshButton : ImageButton
    private lateinit var loadingIcon : ProgressBar

    private var mapReady = false
    private var defaultLat = 34.2407
    private var defaultLng = -118.5300
    private var setLocation = LatLng(defaultLat, defaultLng)
    private var setZoomLevel = 13.0f

    private var searchRadius = 4.0

    private val loadedStations = mutableListOf<Station>()
    private val loadedCustomStations = mutableListOf<Int>()
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

        val style = context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_dark_theme) }
        if (context?.resources?.configuration?.uiMode == 33) {
            googleMap.setMapStyle(style)
        }

        loadingIcon = view?.findViewById(R.id.loading)!!
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(setLocation, setZoomLevel))

        clusterManager = ClusterManager(context, googleMap)
        val clusterRenderer = ClusterRenderer(requireContext(), googleMap, clusterManager)
        clusterManager.renderer = clusterRenderer

        clusterManager.markerCollection.setInfoWindowAdapter(context?.let { CustomInfoWindow(it) })

        loadNearestStations(setLocation, searchRadius)
        loadNearestCustomStations()

        /*googleMap.setOnCameraMoveStartedListener {
            clusterManager.markerCollection.markers.forEach { it.alpha = 0.3f }
            clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.3f }
        }*/

        googleMap.setOnCameraIdleListener {
            //clusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            //`clusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }

            val center = googleMap.cameraPosition.target
            val zoom = googleMap.cameraPosition.zoom
            searchRadius = min(16.0, 2.0.pow((14.5 - zoom)))
            loadNearestStations(center, searchRadius)
            loadNearestCustomStations()

            Log.d("debug", "camera moved to: " + center.toString())
            Log.d("debug", "search radius: " +  searchRadius.toString())

            clusterManager.onCameraIdle()
        }

        clusterItemInfoWindowClickListener = object : ClusterManager.OnClusterItemInfoWindowClickListener<StationClusterItem> {
            override fun onClusterItemInfoWindowClick(item: StationClusterItem) {
                val id = item.getTag()
                Log.d("debug", "marker $id" + " info Window Clicked")
                val intent = Intent(context, StationInfoActivity::class.java)
                intent.putExtra("id", id.toString())
                Log.d("debug", "launching info for " + id.toString())
                stationInfoLauncher.launch(intent)
            }
        }

        googleMap.setOnMarkerClickListener(clusterManager)
        clusterManager.setOnClusterItemInfoWindowClickListener(clusterItemInfoWindowClickListener)


        refreshButton = view?.findViewById(R.id.refreshButton)!!
        refreshButton.setOnClickListener {
            googleMap.clear()
            clusterManager.clearItems()
            loadedStations.clear()
            loadedCustomStations.clear()
            addCurrentLocation()
            loadNearestStations(googleMap.cameraPosition.target, searchRadius)
            loadNearestCustomStations()
        }
        val filterButton = view?.findViewById<ImageButton>(R.id.filterButton)!!
        filterButton.setOnClickListener {
            openFilterActivity()
        }
        val favoriteButton = view?.findViewById<ImageButton>(R.id.favoriteButton)!!
        favoriteButton.setOnClickListener {
            openFavoriteActivity()
        }

        placesClient = Places.createClient(context)

        val fields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)

        val searchButton = view?.findViewById<ImageButton>(R.id.searchButton)!!
        searchButton.setOnClickListener {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(context)
            searchLauncher.launch(intent)

            //startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        val currentLocationButton = view?.findViewById<ImageButton>(R.id.currentLocationButton)!!
        currentLocationButton.setOnClickListener {
            checkPermissionThenFindCurrentPlace()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshMarkers()
        Log.d("debug", "MapsFragment: " + "onResume")
    }

    private fun refreshMarkers() {
        if(mapReady){
            googleMap.clear()
            clusterManager.clearItems()
            loadedStations.clear()
            loadedCustomStations.clear()
            addCurrentLocation()
            loadNearestStations(googleMap.cameraPosition.target, searchRadius)
            loadNearestCustomStations()
        }
    }

    private fun loadNearestStations(location: LatLng, radius: Double) {
        val latitude = location.latitude
        val longitude = location.longitude
        val url = "https://developer.nrel.gov/api/alt-fuel-stations/v1/nearest.json?api_key=atG74JTz1BziqwmY0hecm8a9J14qTnbUb5SOvjPs&fuel_type=ELEC&latitude=$latitude&longitude=$longitude&radius=$radius&limit=all"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        loadingIcon.visibility = View.VISIBLE
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

            Log.d("debug", "Adding station to cluster manager (Lat, Lng): (${station.latitude}, ${station.longitude})")

            clusterManager.addItem(stationClusterItem)
        }
        clusterManager.cluster()
        loadingIcon.visibility = View.INVISIBLE
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
                    var id : Long = document.data?.get("id") as Long

                    if (lng < southwestLng || lng > northeastLng || loadedCustomStations.contains(id.toInt())) {
                        continue;
                    }

                    loadedCustomStations.add(id.toInt())

                    var address = document.data?.get("Address").toString()
                    var charger = document.data?.get("Charger").toString()
                    var level = document.data?.get("Level").toString()
                    var rate = document.data?.get("Rate").toString()
                    var owner = document.data?.get("Owner").toString()
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
                    clusterManager.addItem(customStationClusterItem)
                }
                clusterManager.cluster()
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occur during the query
            }
    }

    private fun addCurrentLocation() {
        val locationMarker = context?.let { bitmapDescriptorFromVector(it, R.drawable.ic_location_marker) }
        val marker = googleMap.addMarker(MarkerOptions().position(LatLng(defaultLat, defaultLng)))
        marker?.setIcon(locationMarker)
        marker?.tag = "location"
    }

    private fun openFilterActivity() {
        var binaryLevels = ""
        var binaryConnectors = ""
        if(levels.contains("ev_level1_evse_num")) {
            binaryLevels += '1'
        } else {
            binaryLevels += '0'
        }
        if(levels.contains("ev_level2_evse_num")) {
            binaryLevels += '1'
        } else {
            binaryLevels += '0'
        }
        if(levels.contains("ev_dc_fast_num")) {
            binaryLevels += '1'
        } else {
            binaryLevels += '0'
        }

        if(connectors.contains("J1772")) {
            binaryConnectors += '1'
        } else {
            binaryConnectors += '0'
        }
        if(connectors.contains("J1772COMBO")) {
            binaryConnectors += '1'
        } else {
            binaryConnectors += '0'
        }
        if(connectors.contains("TESLA")) {
            binaryConnectors += '1'
        } else {
            binaryConnectors += '0'
        }
        if(connectors.contains("CHADEMO")) {
            binaryConnectors += '1'
        } else {
            binaryConnectors += '0'
        }
        if(connectors.contains("NEMA1450")) {
            binaryConnectors += '1'
        } else {
            binaryConnectors += '0'
        }
        if(connectors.contains("NEMA515")) {
            binaryConnectors += '1'
        } else {
            binaryConnectors += '0'
        }
        if(connectors.contains("NEMA520")) {
            binaryConnectors += '1'
        } else {
            binaryConnectors += '0'
        }

        val intent = Intent(context, FilterActivity::class.java)
        intent.putExtra("levels", binaryLevels)
        intent.putExtra("connectors", binaryConnectors)
        intent.putExtra("private", showPrivate)
        intent.putExtra("inactive", showInactive)
        intent.putExtra("location", setLocation)
        intent.putExtra("zoom", setZoomLevel)
        filterLauncher.launch(intent)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    private var filterLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == 1) {
            // There are no request codes
            val data: Intent? = result.data
            val binaryLevels = data?.getStringExtra("levels")
            val binaryConnectors = data?.getStringExtra("connectors")

            showPrivate = data?.getBooleanExtra("private", true) == true
            showInactive = data?.getBooleanExtra("inactive", true) == true

            levels.clear()
            connectors.clear()

            for(i in 0..2){
                if(binaryLevels?.get(i)  == '1'){
                    levels.add(allLevels[i])
                }
            }
            for(i in 0..6) {
                if (binaryConnectors?.get(i) == '1') {
                    connectors.add(allConnectors[i])
                }
            }
            refreshMarkers()
        }
    }

    private fun openFavoriteActivity() {
        val intent = Intent(context, FavoriteActivity::class.java)
        favoriteLauncher.launch(intent)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    private var favoriteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == 1) {
            refreshMarkers()
        }
    }

    private var searchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        Log.d("debug", ContentValues.TAG +  " place: ${place.name}, ${place.id}")

                        googleMap.clear()
                        loadedStations.clear()
                        //googleMap.addMarker(MarkerOptions().position(latLng).title(location))
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(place.latLng))
                        //Toast.makeText(requireContext(), address.latitude.toString() + " " + address.longitude, Toast.LENGTH_LONG).show()
                        refreshMarkers()
                    }
                }

                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    private var stationInfoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.resultCode) {

                Activity.RESULT_OK -> {

                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
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

    private fun checkPermissionThenFindCurrentPlace() {
        when {
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) -> {
                // You can use the API that requires the permission.
                findCurrentPlace()
            }
            else -> {
                // Ask for both the ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 9
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        } else if (permissions.toList().zip(grantResults.toList())
                .firstOrNull { (permission, grantResult) ->
                    grantResult == PackageManager.PERMISSION_GRANTED && (permission == Manifest.permission.ACCESS_FINE_LOCATION || permission == Manifest.permission.ACCESS_COARSE_LOCATION)
                } != null
        )
        // At least one location permission has been granted, so proceed with Find Current Place
            findCurrentPlace()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun findCurrentPlace() {
        // Use fields to define the data types to return.
        val placeFields: List<Place.Field> =
            listOf(Place.Field.NAME, Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        // Use the builder to create a FindCurrentPlaceRequest.
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // Retrieve likely places based on the device's current location
            lifecycleScope.launch {
                try {

                    val response = placesClient.awaitFindCurrentPlace(placeFields)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(response.placeLikelihoods[0].place.latLng))
                    defaultLat = response.placeLikelihoods[0].place.latLng.latitude;
                    defaultLng = response.placeLikelihoods[0].place.latLng.longitude;
                    setLocation = LatLng(defaultLat, defaultLng)
                    addCurrentLocation();


                } catch (e: Exception) {

                    e.printStackTrace()

                }
            }
        } else {
            Log.d("debug", ContentValues.TAG + ": LOCATION permission not granted")
            checkPermissionThenFindCurrentPlace()

        }
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