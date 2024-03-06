@file:Suppress("DEPRECATION")

package com.example.chatapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.adapter.MessageAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var messages: ArrayList<Message>
    private lateinit var sender: String
    private lateinit var receiver: String
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private var dialog: ProgressDialog? = null
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var name: String
    private lateinit var profileImg: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContentView(binding.root)
        chatFn()
        binding.imgCamera.setOnClickListener {
            Toast.makeText(this@ChatActivity, "Feature coming soon...", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("KotlinConstantConditions")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25 && resultCode == Activity.RESULT_OK){
            if (data != null){
                if (data.data != null){
                    val selectedImage = data.data
                    val calender = Calendar.getInstance()
                    val reference = storage.reference.child("chats").child(calender.timeInMillis.toString()+"")
                    dialog!!.show()
                    reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                        dialog!!.dismiss()
                        if (task.isSuccessful){
                            reference.downloadUrl.addOnCompleteListener { uri ->
                                val filePath = uri.result.toString()
                                val messageTxt: String = binding.etMsg.text.toString()
                                val date = Date()
                                val message = Message(messageTxt, senderUid, date.time)
                                message.message = "photo"
                                message.imageUrl = filePath
                                val randomKey = database.reference.push().key.toString()
                                val lastMsgObj = HashMap<String, Any>()
                                lastMsgObj["lastMsg"] = message.message
                                lastMsgObj["lastMsgTime"] = date.time
                                database.reference.child("chats").updateChildren(lastMsgObj)
                                database.reference.child("chats").child(receiver).updateChildren(lastMsgObj)
                                database.reference.child("chats").child(sender).child("messages").child(randomKey).setValue(message).addOnCompleteListener {
                                    database.reference.child("chats").child(receiver).child("messages").child(randomKey).setValue(message).addOnCompleteListener {
                                        binding.etMsg.setText("")
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@ChatActivity, "Task Exception ${task.exception}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@ChatActivity, "${data.data} is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@ChatActivity, "$data is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun init(){
        binding = ActivityChatBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Uploading Image")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        name = intent.getStringExtra("name").toString()
        profileImg = intent.getStringExtra("image").toString()
    }

    private fun chatFn(){
        binding.txtName.text = name
        Glide.with(this@ChatActivity).load(profileImg).placeholder(R.drawable.placeholder).into(binding.imgProfile)
        binding.imgBack.setOnClickListener {
            finish()
        }
        receiverUid = intent.getStringExtra("uid").toString()
        senderUid = auth.uid!!
        database.reference.child("Presence").child(receiverUid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val status = snapshot.getValue(String::class.java)
                    if (status == "offline"){
                        binding.txtStatus.visibility = View.GONE
                    } else {
                        binding.txtStatus.text = status
                        binding.txtStatus.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
        sender = senderUid + receiverUid
        receiver = receiverUid + senderUid
        adapter = MessageAdapter(this@ChatActivity, messages, sender, receiver)
        binding.rcChat.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding.rcChat.adapter = adapter
        database.reference.child("chats").child(sender).child("messages").addValueEventListener(object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (snapshot1 in snapshot.children){
                    val message :Message? = snapshot1.getValue(Message::class.java)
                    message!!.messageId = snapshot1.key.toString()
                    messages.add(message)
                }
                adapter.notifyDataSetChanged()
                binding.rcChat.scrollToPosition(messages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
        binding.imgSend.setOnClickListener {
            if (binding.etMsg.text.isEmpty()){
                Toast.makeText(this@ChatActivity, "Cannot send empty message", Toast.LENGTH_SHORT).show()
            } else {
                val messageTxt: String = binding.etMsg.text.toString()
                val date = Date()
                val message = Message(messageTxt, senderUid, date.time)
                val randomKey = database.reference.push().key.toString()
                val lastMsgObj = HashMap<String, Any>()
                lastMsgObj["lastMsg"] = message.message
                lastMsgObj["lastMsgTime"] = date.time
                database.reference.child("chats").child(sender).updateChildren(lastMsgObj)
                database.reference.child("chats").child(receiver).updateChildren(lastMsgObj)
                database.reference.child("chats").child(sender).child("messages").child(randomKey).setValue(message).addOnCompleteListener {
                    database.reference.child("chats").child(receiver).child("messages").child(randomKey).setValue(message).addOnCompleteListener {
                        binding.etMsg.setText("")
                    }
                }
            }
        }
        binding.imgPin.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }
        val handler = Handler()
        binding.etMsg.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                database.reference.child("Presence").child(senderUid).setValue("Typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }
            var userStoppedTyping = Runnable {
                database.reference.child("Presence").child(senderUid).setValue("Online")
            }
        })
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onPause() {
        super.onPause()
        val currentId = auth.uid.toString()
        database.reference.child("Presence").child(currentId).setValue("Offline")
    }

    override fun onResume() {
        super.onResume()
        val currentId = auth.uid.toString()
        database.reference.child("Presence").child(currentId).setValue("Online")
    }
}