package com.example.evchargingapp

import com.google.android.gms.maps.model.LatLng

class StationClusterItem(
    private val position: LatLng,
    private val title: String,
    private val snippet: String,
    private val tag: Any
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
}
