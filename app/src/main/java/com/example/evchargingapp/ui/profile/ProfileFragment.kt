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
import com.example.evchargingapp.AddCustomStation
import com.example.evchargingapp.LoginActivity
import com.example.evchargingapp.R
import com.example.evchargingapp.rentHomeCharging
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment() {

    private lateinit var sp: SharedPreferences
    private lateinit var usernameText : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        usernameText = view.findViewById(R.id.UsernameText)
        val rentMyChargerButton = view.findViewById<Button>(R.id.RentMyChargerButton)
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
            val intent = Intent(context, rentHomeCharging::class.java)
            resultLauncher.launch(intent)
        }

        logOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
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
        } else if (result.resultCode == 2) {
            val navController = findNavController()
            navController.navigate(R.id.navigation_map)
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

    override fun onResume() {
        super.onResume()
        val username = sp.getString("username", "")
        val password = sp.getString("password", "")
        if((username == "" || password == "")){
            val intent = Intent(context, LoginActivity::class.java)
            resultLauncher.launch(intent)
        } else {
            usernameText.text = username;
        }
    }

}