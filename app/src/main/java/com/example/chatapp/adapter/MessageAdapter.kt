package com.example.chatapp.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.databinding.DeleteLayoutBinding
import com.example.chatapp.databinding.ReceiveMessageBinding
import com.example.chatapp.databinding.SendMessageBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Suppress("JoinDeclarationAndAssignment")
class MessageAdapter(private var context: Context, private var messageList: ArrayList<Message>, sender: String, receiver: String):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages: ArrayList<Message>
    private var auth: FirebaseAuth
    private var database: FirebaseDatabase
    private val itemSent = 1
    private val itemReceive = 2
    private val sender: String
    private val receiver: String

    inner class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding: SendMessageBinding = SendMessageBinding.bind(itemView)
    }

    inner class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding: ReceiveMessageBinding = ReceiveMessageBinding.bind(itemView)
    }

    init {
        this.messages = messageList
        this.sender = sender
        this.receiver = receiver
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == itemSent){
            val view = LayoutInflater.from(context).inflate(R.layout.send_message, parent, false)
            SentMessageHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_message, parent, false)
            ReceivedMessageHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = messageList[position]
        return if (auth.uid == messages.senderId){
            itemSent
        } else {
            itemReceive
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
//        if (holder.javaClass == SentMessageHolder::class.java) {
//            val viewHolder = holder as SentMessageHolder
//            if (message.message == "photo") {
//                viewHolder.binding.imgMsg.visibility = View.VISIBLE
//                viewHolder.binding.txtMsg.visibility = View.GONE
//                viewHolder.binding.llSend.visibility = View.GONE
//                Glide.with(context).load(message.imageUrl).placeholder(R.drawable.placeholder)
//                    .into(viewHolder.binding.imgMsg)
//                viewHolder.binding.txtMsg.text = message.message
//                viewHolder.itemView.setOnLongClickListener {
//                    val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
//                    val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
//                    val dialog = AlertDialog.Builder(context).setTitle("Delete Message")
//                        .setView(binding.root).create()
//                    binding.txtDel.setOnClickListener {
//                        message.message = "This message has been deleted"
//                        message.messageId.let { it1 ->
//                            database.reference.child("chats").child(sender).child("message")
//                                .child(it1).setValue(message)
//                        }
//                        message.messageId.let { it1 ->
//                            database.reference.child("chats").child(receiver).child("message")
//                                .child(it1).setValue(message)
//                        }
//                        dialog.dismiss()
//                    }
//                    binding.txtCancel.setOnClickListener {
//                        dialog.dismiss()
//                    }
//                    dialog.show()
//                    false
//                }
//            } else {
//                val viewHolder = holder as ReceivedMessageHolder
//                if (message.message == "photo") {
//                    viewHolder.binding.imgMsg.visibility = View.VISIBLE
//                    viewHolder.binding.txtMsg.visibility = View.GONE
//                    viewHolder.binding.llReceive.visibility = View.GONE
//                    Glide.with(context).load(message.imageUrl).placeholder(R.drawable.placeholder).into(viewHolder.binding.imgMsg)
//                    viewHolder.binding.txtMsg.text = message.message
//                    viewHolder.itemView.setOnLongClickListener {
//                        val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
//                        val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
//                        val dialog = AlertDialog.Builder(context).setTitle("Delete Message").setView(binding.root).create()
//                        binding.txtDel.setOnClickListener {
//                            message.message = "This message has been deleted"
//                            message.messageId.let { it1 ->
//                                database.reference.child("chats").child(sender).child("message").child(it1).setValue(message)
//                            }
//                            message.messageId.let { it1 ->
//                                database.reference.child("chats").child(receiver).child("message").child(it1).setValue(message)
//                            }
//                            dialog.dismiss()
//                        }
//                        binding.txtCancel.setOnClickListener {
//                            dialog.dismiss()
//                        }
//                        dialog.show()
//                        false
//                    }
//                }
//            }
//        }
        when (holder.itemViewType){
            itemSent -> {
                val viewHolder = holder as SentMessageHolder
                viewHolder.binding.txtMsg.text = message.message
                if (message.message == "photo") {
                    viewHolder.binding.imgMsg.visibility = View.VISIBLE
                    viewHolder.binding.txtMsg.visibility = View.GONE
                    viewHolder.binding.llSend.visibility = View.GONE
                    Glide.with(context).load(message.imageUrl).placeholder(R.drawable.placeholder).into(viewHolder.binding.imgMsg)

                } else {
                    viewHolder.binding.imgMsg.visibility = View.GONE
                    viewHolder.binding.txtMsg.visibility = View.VISIBLE
                    viewHolder.binding.llSend.visibility = View.VISIBLE
                    viewHolder.binding.txtMsg.text = message.message
                }
                viewHolder.itemView.setOnLongClickListener {
                    val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                    val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                    val dialog = AlertDialog.Builder(context).setTitle("Delete Message")
                        .setView(binding.root).create()
                    binding.txtDel.setOnClickListener {
                        message.message = "This message has been deleted"
                        message.messageId.let { it1 ->
                            database.reference.child("chats").child(sender).child("message")
                                .child(it1).setValue(message)
                        }
                        message.messageId.let { it1 ->
                            database.reference.child("chats").child(receiver).child("message")
                                .child(it1).setValue(message)
                        }
                        dialog.dismiss()
                    }
                    binding.txtCancel.setOnClickListener {
                        dialog.dismiss()
                    }
                    dialog.show()
                    false
                }
            }
            itemReceive -> {
                val viewHolder = holder as ReceivedMessageHolder
                if (message.message == "photo") {
                    viewHolder.binding.imgMsg.visibility = View.VISIBLE
                    viewHolder.binding.txtMsg.visibility = View.GONE
                    viewHolder.binding.llReceive.visibility = View.GONE
                    Glide.with(context).load(message.imageUrl).placeholder(R.drawable.placeholder).into(viewHolder.binding.imgMsg)
                } else {
                    viewHolder.binding.imgMsg.visibility = View.GONE
                    viewHolder.binding.txtMsg.visibility = View.VISIBLE
                    viewHolder.binding.llReceive.visibility = View.VISIBLE
                    viewHolder.binding.txtMsg.text = message.message
                }

                viewHolder.itemView.setOnLongClickListener {
                    val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                    val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                    val dialog = AlertDialog.Builder(context).setTitle("Delete Message").setView(binding.root).create()
                    binding.txtDel.setOnClickListener {
                        message.message = "This message has been deleted"
                        message.messageId.let { it1 ->
                            database.reference.child("chats").child(sender).child("message").child(it1).setValue(message)
                        }
                        message.messageId.let { it1 ->
                            database.reference.child("chats").child(receiver).child("message").child(it1).setValue(message)
                        }
                        dialog.dismiss()
                    }
                    binding.txtCancel.setOnClickListener {
                        dialog.dismiss()
                    }
                    dialog.show()
                    false
                }
            }
        }
    }

}