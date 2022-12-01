package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.evchargingapp.databinding.ActivityLoginBinding
import com.example.evchargingapp.databinding.ActivityWelcomePageBinding
import com.google.firebase.auth.FirebaseAuth

class WelcomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomePageBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)


     //   binding.textViewSignUp.setOnClickListener {
       //     startActivity(Intent(this, RegisterActivity::class.java))
     //   }

    }





}