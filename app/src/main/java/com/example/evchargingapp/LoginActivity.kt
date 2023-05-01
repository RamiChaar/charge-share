package com.example.evchargingapp

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.evchargingapp.databinding.ActivityLoginBinding
import com.example.evchargingapp.databinding.ActivityMainBinding
import com.example.evchargingapp.databinding.ActivityRegisterBinding
import com.example.evchargingapp.ui.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = Firebase.auth;
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.login_nav_view)
        bottomNavigationView.selectedItemId = R.id.navigation_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_map -> {
                    val returnIntent = Intent()
                    setResult(2, returnIntent)
                    finish()
                    true
                }
                R.id.navigation_profile -> {
                    true
                }
                else -> false
            }
        }

        binding.btnlogin.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val pass = binding.inputPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty() ) {

                firebaseAuth.signInWithEmailAndPassword(email , pass).addOnCompleteListener{
                    if (it.isSuccessful) {
                        val returnIntent = Intent()

                        returnIntent.putExtra("username", email)
                        returnIntent.putExtra("password", pass)

                        setResult(1, returnIntent)
                        finish()
                    }else{
                        Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !", Toast.LENGTH_SHORT).show()
            }

        }
        binding.textViewSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }
}