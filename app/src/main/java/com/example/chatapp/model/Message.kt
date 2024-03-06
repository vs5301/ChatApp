package com.example.chatapp.model

@Suppress("unused", "RemoveEmptySecondaryConstructorBody")
class Message {

    var messageId: String = null.toString()
    lateinit var message: String
    lateinit var senderId: String
    var imageUrl: String = null.toString()
    private var timeStamp: Long = 0
    constructor(){}
    constructor(
        message: String,
        senderId: String,
        timeStamp: Long
    ){
        this.message = message
        this.senderId = senderId
        this.timeStamp = timeStamp
    }
}