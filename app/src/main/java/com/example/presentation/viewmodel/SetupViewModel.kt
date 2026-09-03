package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.local.UserPreferences
import com.example.permissions.PermissionManager
import com.example.permissions.PermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SetupViewModel(
    private val permissionManager: PermissionManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _permissions = MutableStateFlow(permissionManager.getPermissionStatus())
    val permissions: StateFlow<PermissionStatus> = _permissions.asStateFlow()

    fun refreshPermissions() {
        _permissions.value = permissionManager.getPermissionStatus()
    }

    fun completeSetup() {
        userPreferences.setSetupCompleted(true)
    }

    fun isSetupCompleted(): Boolean {
        return userPreferences.isSetupCompleted()
    }

    fun hasSeenOnboarding(): Boolean {
        return userPreferences.hasSeenOnboarding()
    }

    fun completeOnboarding() {
        userPreferences.setSeenOnboarding(true)
    }
}
