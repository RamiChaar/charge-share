package com.example.evchargingapp

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class StationClusterItem(
    private val position: LatLng,
    private val title: String,
    private val snippet: String,
    private val tag: Any,
    var markerIcon: BitmapDescriptor? = null
) : ClusterItem {
    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String {
        return title
    }

    override fun getSnippet(): String {
        return snippet
    }

    fun getTag(): Any {
        return tag
    }

    @JvmName("getMarkerIcon1")
    fun getMarkerIcon(): BitmapDescriptor? {
        return markerIcon
    }
}
