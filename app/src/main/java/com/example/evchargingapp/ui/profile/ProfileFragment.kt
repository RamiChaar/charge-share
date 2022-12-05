package com.example.evchargingapp.ui.profile

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.evchargingapp.LoginActivity
import com.example.evchargingapp.R


class ProfileFragment : Fragment() {

    private lateinit var sp: SharedPreferences
    private lateinit var usernameText : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        usernameText = view.findViewById<TextView>(R.id.UsernameText)
        val rentMyChargerButton = view.findViewById<Button>(R.id.RentMyChargerButton)
        val reportButton = view.findViewById<Button>(R.id.ReportButton)
        val addCarButton = view.findViewById<Button>(R.id.AddCarButton)
        val logOutButton = view.findViewById<Button>(R.id.LogOutButton)

        sp = container?.context?.getSharedPreferences("Login", MODE_PRIVATE) as SharedPreferences
        val username = sp.getString("username", "")
        val password = sp.getString("password", "")
        if((username == "" || password == "")){
            val intent = Intent(context, LoginActivity::class.java)
            resultLauncher.launch(intent)
        } else {
            usernameText.text = username;
        }

        rentMyChargerButton.setOnClickListener {
            //TO DO
        }

        reportButton.setOnClickListener {
            //TO DO
        }

        addCarButton.setOnClickListener {
            //TO DO
        }

        logOutButton.setOnClickListener {
            clearLoginCredentials()
            val intent = Intent(context, LoginActivity::class.java)
            resultLauncher.launch(intent)
        }

        return view
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == 1) {
            val data: Intent? = result.data
            val username = data?.getStringExtra("username")
            val password = data?.getStringExtra("password")
            usernameText.text = username;
            if (username != null && password != null) {
                storeLoginCredentials(username, password)
            }

        }
    }

    private fun storeLoginCredentials(username : String, password : String) {
        val ed = sp.edit()
        ed.putString("username", username)
        ed.putString("password", password)
        ed.apply()
    }

    private fun clearLoginCredentials(){
        sp.edit().clear().apply();
    }

}