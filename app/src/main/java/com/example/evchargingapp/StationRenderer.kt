import android.content.Context
import android.util.Log
import com.example.evchargingapp.R
import com.example.evchargingapp.Station
import com.example.evchargingapp.bitmapDescriptorFromVector
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class StationRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Station>
) : DefaultClusterRenderer<Station>(context, map, clusterManager) {

    private val defaultMarker = context.bitmapDescriptorFromVector(R.drawable.ic_resource_default_marker)
    private val customMarker = context.bitmapDescriptorFromVector(R.drawable.ic_resource_custom_marker)
    private val inactiveMarker = context.bitmapDescriptorFromVector(R.drawable.ic_resource_inactive_marker)
    private val privateMarker = context.bitmapDescriptorFromVector(R.drawable.ic_resource_private_marker)
    private val levelOneMarker = context.bitmapDescriptorFromVector(R.drawable.ic_resource_level1_marker)
    private val levelTwoMarker = context.bitmapDescriptorFromVector(R.drawable.ic_resource_level2_marker)
    private val levelThreeMarker = context.bitmapDescriptorFromVector(R.drawable.ic_resource_level3_marker)

    override fun onBeforeClusterItemRendered(item: Station, markerOptions: MarkerOptions) {
        Log.e("station Render", item.toString())
        var snippetString = ""
        if (item.status_code != "E") {
            markerOptions.icon(inactiveMarker)
            snippetString += "Status: Inactive"
        } else if (item.access_code == "custom") {
            markerOptions.icon(customMarker)
            snippetString += "Address: 15764 Larkspur St, Sylmar, CA 91342"
            snippetString += "\nStatus: Active (custom)"
            snippetString += "\nAccess: Public"
            snippetString += "\nLevel: Level 2"
        } else if (item.access_code == "private") {
            markerOptions.icon(privateMarker)
            snippetString += "Status: Active"
            snippetString += "\nAccess: Private"
            snippetString += "\nLevel: Fast (Level3)"
        } else if (item.ev_dc_fast_num > 0) {
            markerOptions.icon(levelThreeMarker)
            snippetString += "Status: Active"
            snippetString += "\nAccess: Public"
            snippetString += "\nLevel: Fast (Level3)"
        } else if (item.ev_level2_evse_num > 0) {
            markerOptions.icon(levelTwoMarker)
            snippetString += "Status: Active"
            snippetString += "\nAccess: Public"
            snippetString += "\nLevel: Level 2"
        } else if (item.ev_level1_evse_num > 0) {
            markerOptions.icon(levelOneMarker)
            snippetString += "Status: Active"
            snippetString += "\nAccess: Public"
            snippetString += "\nLevel: Level 1"
        } else {
            markerOptions.icon(defaultMarker)
            snippetString += "Details Unknown"
        }

        markerOptions.position(LatLng(item.latitude, item.longitude))
            .title(item.station_name)
            .snippet(snippetString)
    }

    override fun onClusterItemRendered(clusterItem: Station, marker: Marker) {
        super.onClusterItemRendered(clusterItem, marker)
        marker.tag = clusterItem.id
    }
}
