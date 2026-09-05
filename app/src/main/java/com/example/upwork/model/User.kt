package com.example.upwork.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student" // Default role
)
