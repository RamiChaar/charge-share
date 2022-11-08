package com.example.evchargingapp

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat


class FilterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        val backButton = findViewById<Button>(R.id.backButton)
        val private = findViewById<SwitchCompat>(R.id.showPrivate)
        val inactive = findViewById<SwitchCompat>(R.id.showInactive)
        val level1 = findViewById<SwitchCompat>(R.id.level1)
        val level2 = findViewById<SwitchCompat>(R.id.level2)
        val level3 = findViewById<SwitchCompat>(R.id.level3)
        val J1772 = findViewById<SwitchCompat>(R.id.J1772)
        val J1772COMBO = findViewById<SwitchCompat>(R.id.J1772COMBO)
        val TESLA = findViewById<SwitchCompat>(R.id.TESLA)
        val CHADEMO = findViewById<SwitchCompat>(R.id.CHADEMO)
        val NEMA1450 = findViewById<SwitchCompat>(R.id.NEMA1450)
        val NEMA515 = findViewById<SwitchCompat>(R.id.NEMA515)
        val NEMA520 = findViewById<SwitchCompat>(R.id.NEMA520)

        val intent = intent
        val allLevels = mutableListOf("ev_level1_evse_num", "ev_level2_evse_num", "ev_dc_fast_num")
        val allConnectors = mutableListOf("J1772", "J1772COMBO", "TESLA", "CHADEMO", "NEMA1450", "NEMA515", "NEMA520")
        val binaryLevels = intent.getStringExtra("levels")
        val binaryConnectors = intent.getStringExtra("connectors")
        var showPrivate = intent.getBooleanExtra("private", true)
        var showInactive = intent.getBooleanExtra("inactive", true)

        val levels = mutableListOf<String>()
        val connectors = mutableListOf<String>()

        for(i in 0..2){
            if(binaryLevels?.get(i)  == '1'){
                levels.add(allLevels[i])
            }
        }
        for(i in 0..6){
            if(binaryConnectors?.get(i)  == '1'){
                connectors.add(allConnectors[i])
            }
        }

        if(showPrivate){
            private.isChecked = true
        }
        if(showInactive){
            inactive.isChecked = true
        }

        if(levels.contains("ev_level1_evse_num")){
            level1.isChecked = true
        }
        if(levels.contains("ev_level2_evse_num")){
            level2.isChecked = true
        }
        if(levels.contains("ev_dc_fast_num")){
            level3.isChecked = true
        }

        if(connectors.contains("J1772")){
            J1772.isChecked = true
        }
        if(connectors.contains("J1772COMBO")){
            J1772COMBO.isChecked = true
        }
        if(connectors.contains("TESLA")){
            TESLA.isChecked = true
        }
        if(connectors.contains("CHADEMO")){
            CHADEMO.isChecked = true
        }
        if(connectors.contains("NEMA1450")){
            NEMA1450.isChecked = true
        }
        if(connectors.contains("NEMA515")){
            NEMA515.isChecked = true
        }
        if(connectors.contains("NEMA520")){
            NEMA520.isChecked = true
        }

        private.setOnCheckedChangeListener { _, switchedOn ->
            showPrivate = switchedOn
        }

        inactive.setOnCheckedChangeListener { _, switchedOn ->
            showInactive = switchedOn
        }

        level1.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                levels.add("ev_level1_evse_num")
            }
            else {
                levels.remove("ev_level1_evse_num")
            }
        }
        level2.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                levels.add("ev_level2_evse_num")
            }
            else {
                levels.remove("ev_level2_evse_num")
            }
        }
        level3.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                levels.add("ev_dc_fast_num")
            }
            else {
                levels.remove("ev_dc_fast_num")
            }
        }

        J1772.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                connectors.add("J1772")
            }
            else {
                connectors.remove("J1772")
            }
        }
        J1772COMBO.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                connectors.add("J1772COMBO")
            }
            else {
                connectors.remove("J1772COMBO")
            }
        }
        TESLA.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                connectors.add("TESLA")
            }
            else {
                connectors.remove("TESLA")
            }
        }
        CHADEMO.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                connectors.add("CHADEMO")
            }
            else {
                connectors.remove("CHADEMO")
            }
        }
        NEMA1450.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                connectors.add("NEMA1450")
            }
            else {
                connectors.remove("NEMA1450")
            }
        }
        NEMA515.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
            }
            else {
                connectors.remove("NEMA515")
            }
        }
        NEMA520.setOnCheckedChangeListener { _, switchedOn ->
            if (switchedOn) {
                connectors.add("NEMA520")
            }
            else {
                connectors.remove("NEMA520")
            }
        }

        backButton.setOnClickListener{
            val returnIntent = Intent()
            val binaryLevelsReturn = getBinaryLevels(levels)
            val binaryConnectorsReturn = getBinaryConnectors(connectors)
            returnIntent.putExtra("levels", binaryLevelsReturn)
            returnIntent.putExtra("connectors", binaryConnectorsReturn)
            returnIntent.putExtra("private", showPrivate)
            returnIntent.putExtra("inactive", showInactive)
            setResult(1, returnIntent)
            finish()
        }
    }

    private fun getBinaryLevels(levels: MutableList<String>): String {
        var binaryLevels = ""
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
        return binaryLevels
    }

    private fun getBinaryConnectors(connectors: MutableList<String>): String {
        var binaryConnectors = ""

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
        return binaryConnectors
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}