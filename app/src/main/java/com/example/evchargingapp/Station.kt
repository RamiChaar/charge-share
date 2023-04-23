package com.example.evchargingapp

class Station(val id: Int, val latitude : Double, val longitude : Double, var station_name : String, var status_code : String, var access_code : String, var ev_connector_types : List<String>, var ev_level1_evse_num : Int, var ev_level2_evse_num : Int, var ev_dc_fast_num : Int) {

}