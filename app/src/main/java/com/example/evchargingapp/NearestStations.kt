package com.example.evchargingapp

class NearestStations (var latitude : Double, var longitude : Double, var total_results : Int, val fuel_stations: List<Station>) {

}

//api key: atG74JTz1BziqwmY0hecm8a9J14qTnbUb5SOvjPs
//base url: https://developer.nrel.gov/api/alt-fuel-stations/

//get by id:
// v1/{id}.json?api_key={key}

//get nearest stations, adjust radius(miles):
// v1/nearest.json?api_key={key}&latitude={latitude}&longitude={longitude}&radius={radius}&limit=5

//query all stations:
// v1.json?api_key={key}