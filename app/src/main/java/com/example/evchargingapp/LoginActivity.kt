package com.example.evchargingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.evchargingapp.databinding.ActivityLoginBinding
import com.example.evchargingapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnlogin.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val pass = binding.inputPassword.text.toString()


            if (email.isNotEmpty() && pass.isNotEmpty() ) {


                    firebaseAuth.signInWithEmailAndPassword(email , pass).addOnCompleteListener{
                        if (it.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
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