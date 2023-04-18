package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.evchargingapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.AttributedCharacterIterator

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val username = binding.inputPass.text.toString()
            val email = binding.inputEmail.text.toString()
            val pass = binding.inputPassword.text.toString()
            val confirmPass = binding.inputConfirmPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {

                    firebaseAuth.createUserWithEmailAndPassword(email , pass).addOnCompleteListener{
                        if (it.isSuccessful) {
                          finish()
                        }else{
                            Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

                        }
                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Empty Fields Are not Allowed !", Toast.LENGTH_SHORT).show()
            }
        }
        binding.alreadyHaveAccount.setOnClickListener {
            finish()
        }
    }
}