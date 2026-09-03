package com.example.data.repository

import com.example.data.local.UserPreferences
import com.example.data.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(): UserProfile
    suspend fun saveUserProfile(profile: UserProfile): Boolean
    suspend fun updateAssistantName(name: String): Boolean
    suspend fun updateUserName(name: String): Boolean
    suspend fun updateSettings(language: String, theme: String, speechRate: Float): Boolean
    suspend fun resetUserData(): Boolean
}

class LocalUserRepository(
    private val userPreferences: UserPreferences
) : UserRepository {

    override suspend fun getUserProfile(): UserProfile {
        return userPreferences.getUserProfile()
    }

    override suspend fun saveUserProfile(profile: UserProfile): Boolean {
        userPreferences.cacheUserProfile(profile)
        return true
    }

    override suspend fun updateAssistantName(name: String): Boolean {
        userPreferences.setAssistantName(name)
        return true
    }

    override suspend fun updateUserName(name: String): Boolean {
        userPreferences.setUserDisplayName(name)
        return true
    }

    override suspend fun updateSettings(
        language: String,
        theme: String,
        speechRate: Float
    ): Boolean {
        userPreferences.setLanguage(language)
        userPreferences.setTheme(theme)
        userPreferences.setSpeechRate(speechRate)
        return true
    }

    override suspend fun resetUserData(): Boolean {
        userPreferences.clearUserData()
        return true
    }
}

