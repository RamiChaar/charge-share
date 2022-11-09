package com.example.evchargingapp.ui.profile

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.evchargingapp.LoginActivity
import com.example.evchargingapp.R


class ProfileFragment : Fragment() {

    private lateinit var sp: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        sp = container?.context?.getSharedPreferences("Login", MODE_PRIVATE) as SharedPreferences

        val username = sp.getString("username", "")
        val password = sp.getString("password", "")

        if(username == "" || password == ""){
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