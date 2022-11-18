package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.evchargingapp.databinding.ActivityLoginBinding
import com.example.evchargingapp.databinding.ActivityRegisterBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnlogin.setOnClickListener {
        }
        binding.textViewSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }
}