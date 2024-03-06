package com.example.chatapp.model

@Suppress("unused", "RemoveEmptySecondaryConstructorBody", "MemberVisibilityCanBePrivate")
class User {
    var uid: String = ""
    var name: String = ""
    lateinit var email: String
    lateinit var profileImg: String
    constructor(){}
    constructor(
        uid: String,
        name: String,
        email: String,
        profileImg: String
    ) {
        this.uid = uid
        this.name = name
        this.email = email
        this.profileImg = profileImg
    }
}