package com.example.data.model

data class ContactInfo(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val lookupKey: String? = null
)
