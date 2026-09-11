package com.example.upwork.model

import java.security.Timestamp

data class User(
    val email: String = "",
    val role: String = "student",
    val name: String = "",
    val createdAt: Timestamp? = null
)