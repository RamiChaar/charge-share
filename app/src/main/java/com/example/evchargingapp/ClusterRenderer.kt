package com.example.evchargingapp

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class ClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<StationClusterItem>
) : DefaultClusterRenderer<StationClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: StationClusterItem, markerOptions: MarkerOptions) {
        markerOptions.icon(item.getMarkerIcon())
        markerOptions.snippet(item.snippet)
        markerOptions.title(item.title)
        //super.onBeforeClusterItemRendered(item, markerOptions)
    }

    override fun onClusterItemRendered(clusterItem: StationClusterItem, marker: Marker) {
        marker.tag = clusterItem
    }

    override fun shouldRenderAsCluster(cluster: Cluster<StationClusterItem>): Boolean {
        return cluster.size >= 5
    }
}
