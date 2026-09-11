package com.example.upwork.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class Video(
    val videoId: String = "",
    val title: String = "",
    val instructorId: String = "",
    val createdAt: Timestamp? = null
) : Serializable
