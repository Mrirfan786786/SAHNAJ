package com.example.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val assistantName: String = "SAHNAJ",
    val language: String = "Hinglish",
    val theme: String = "SYSTEM",
    val speechRate: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val plan: String = "FREE",
    val isActive: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "displayName" to displayName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "photoUrl" to photoUrl,
            "assistantName" to assistantName,
            "language" to language,
            "theme" to theme,
            "speechRate" to speechRate,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "plan" to plan,
            "isActive" to isActive
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): UserProfile {
            return UserProfile(
                uid = map["uid"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "",
                email = map["email"] as? String ?: "",
                phoneNumber = map["phoneNumber"] as? String ?: "",
                photoUrl = map["photoUrl"] as? String ?: "",
                assistantName = (map["assistantName"] as? String)?.uppercase() ?: "SAHNAJ",
                language = map["language"] as? String ?: "Hinglish",
                theme = map["theme"] as? String ?: "SYSTEM",
                speechRate = (map["speechRate"] as? Number)?.toFloat() ?: 1.0f,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                plan = map["plan"] as? String ?: "FREE",
                isActive = map["isActive"] as? Boolean ?: true
            )
        }
    }
}
