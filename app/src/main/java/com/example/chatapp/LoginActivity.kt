package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContentView(binding.root)
        binding.btnContinue.setOnClickListener {
            loginUser()
        }
        binding.txtRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun init(){
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        binding = ActivityLoginBinding.inflate(layoutInflater)
    }

    private fun loginUser(){
        val email = binding.etEmail.text.toString()
        val password = binding.etPass.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    database.reference.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                for (userSnapshot in snapshot.children){
                                    val userId = userSnapshot.child("email").value.toString()
                                    if (userId == email){
                                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                        break
                                    } else {
                                        val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            } else {
                                val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_SHORT).show()
                        }

                    })
                } else {
                    Toast.makeText(this, it.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this@LoginActivity, "User not found, please check login details", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please enter all values", Toast.LENGTH_SHORT).show()
        }
    }
}