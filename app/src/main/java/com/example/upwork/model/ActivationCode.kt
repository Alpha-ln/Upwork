package com.example.upwork.model

import com.google.firebase.Timestamp

data class ActivationCode(
    val status: String = "active", // "active" | "used"
    val usedByStudentId: String? = null,
    val usedAt: Timestamp? = null,
    val videoId: String = ""
)