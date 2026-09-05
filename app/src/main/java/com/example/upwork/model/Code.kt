package com.example.upwork.model

data class Code(
    val code: String = "",
    val status: Boolean = false, // false = available, true = activated/used
    val verifiedByStudentEmail: String = ""
)
