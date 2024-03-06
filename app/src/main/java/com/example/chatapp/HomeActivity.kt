@file:Suppress("DEPRECATION")

package com.example.chatapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.databinding.ActivityHomeBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

    @Suppress("DEPRECATION")
    class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var users: ArrayList<User>
    private lateinit var usersAdapter: UserAdapter
    private lateinit var layoutManager: GridLayoutManager
    private var dialog: ProgressDialog? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContentView(binding.root)
        getProfile()
        binding.txtLogout.setOnClickListener { 
            auth.signOut()
            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun init(){
        binding = ActivityHomeBinding.inflate(layoutInflater)
        dialog = ProgressDialog(this@HomeActivity)
        dialog?.setMessage("Uploading Image")
        dialog?.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        users = ArrayList()
        usersAdapter = UserAdapter(this@HomeActivity, users)
        layoutManager = GridLayoutManager(this@HomeActivity, 2)
    }

    private fun getProfile(){
        binding.rcRec.layoutManager = layoutManager
        database.reference.child("Users").child(auth.uid!!).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
        binding.rcRec.adapter = usersAdapter
        database.reference.child("Users").addValueEventListener(object :ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (snapshot1 in snapshot.children){
                    user = snapshot1.getValue(User::class.java)
                    if (!user?.uid.equals(auth.uid))
                        users.add(user!!)
                }
                usersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
        database.reference.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children){
                    val userId = userSnapshot.key
                    if (userId == auth.currentUser?.uid){
                        binding.txtName.text = userSnapshot.child("name").getValue(String::class.java)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onResume(){
        super.onResume()
        if (auth.currentUser != null){
            database.reference.child("Presence").child(auth.currentUser!!.uid).setValue("Online")
        } else {
            Toast.makeText(this@HomeActivity, "Login expired, please login again", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause(){
        super.onPause()
        if (auth.currentUser != null){
            database.reference.child("Presence").child(auth.currentUser!!.uid).setValue("Offline")
        } else {
            Toast.makeText(this@HomeActivity, "Login expired, please login again", Toast.LENGTH_SHORT).show()
        }
    }
}