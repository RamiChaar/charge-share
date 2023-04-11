package com.example.evchargingapp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.util.*

data class Station(
    val id: Int,
    val latitude : Double,
    val longitude : Double,
    var station_name : String,
    var street_address: String,
    var status_code : String,
    var access_code : String,
    var ev_connector_types : List<String>,
    var ev_level1_evse_num : Int,
    var ev_level2_evse_num : Int,
    var ev_dc_fast_num : Int
) : ClusterItem {

    override fun getPosition(): LatLng =
        LatLng(latitude, longitude)

    override fun getTitle(): String =
        station_name

    override fun getSnippet(): String =
        street_address

    override fun hashCode(): Int {
        return Objects.hash(id, latitude, longitude, station_name, street_address, status_code, access_code, ev_connector_types, ev_level1_evse_num, ev_level2_evse_num, ev_dc_fast_num)
    }
}
