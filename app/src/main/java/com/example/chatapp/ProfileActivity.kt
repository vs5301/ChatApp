@file:Suppress("DEPRECATION", "NAME_SHADOWING")

package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.ActivityProfileBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContentView(binding.root)
        binding.imgProfile.setOnClickListener {
            getImg()
        }
        binding.btnContinue.setOnClickListener {
            createProfile()
        }
    }

    private fun init(){
        binding = ActivityProfileBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog?.setMessage("Update Profile")
        dialog?.setCancelable(false)
    }

    private fun getImg(){
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 45)
    }

    private fun createProfile(){
        val name = binding.etUser.text.toString()
        if (name.isNotEmpty()){
            dialog?.show()
            val ref = storage.reference.child("Profile").child(auth.currentUser?.email!!)
            ref.putFile(selectedImg).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    ref.downloadUrl.addOnCompleteListener { uri ->
                        val imageUrl = uri.result.toString()
                        val uid = auth.uid
                        val name = binding.etUser.text.toString()
                        val email = auth.currentUser?.email
                        val user = User(uid!!, name, email!!, imageUrl)
                        database.reference.child("Users").child(uid).setValue(user).addOnCompleteListener {
                            dialog?.dismiss()
                            val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    val uid = auth.uid
                    val email = auth.currentUser?.email
                    val name = binding.etUser.text.toString()
                    val user = User(uid!!, name, email!!, "No Image")
                    database.reference.child("Users").child(uid).setValue(user).addOnCompleteListener {
                        dialog?.dismiss()
                        val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        } else {
            binding.etUser.error = "Please enter username"
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (data.data != null) {
//                val uri = data.data
//                val time = Date().time
//                val reference = storage.reference.child("Profile").child(time.toString()+"")
//                reference.putFile(uri!!).addOnCompleteListener { task ->
//                    if (task.isSuccessful){
//                        reference.downloadUrl.addOnCompleteListener { uri ->
//                            val filePath = uri.result.toString()
//                            val obj = HashMap<String, Any>()
//                            obj["image"] = filePath
//                            database.reference.child("Users").child(auth.uid!!).updateChildren(obj).addOnCompleteListener {  }
//                        }
//                    }
//                }
                binding.imgProfile.setImageURI(data.data)
                selectedImg = data.data!!
            }
        }
    }
}