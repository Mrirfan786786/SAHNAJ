package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.repository.LicenseRepository
import com.example.data.repository.LicenseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LicenseUiState(
    val isLicensed: Boolean = false,
    val licenseKey: String = "",
    val activationDate: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class LicenseViewModel(
    private val userPreferences: UserPreferences,
    private val licenseRepository: LicenseRepository = LicenseRepository(userPreferences)
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LicenseUiState(
            isLicensed = userPreferences.isLicensed(),
            licenseKey = userPreferences.getLicenseKey(),
            activationDate = userPreferences.getLicenseActivatedDate()
        )
    )
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    init {
        // Collect local preference changes
        viewModelScope.launch {
            userPreferences.isLicensed.collect { licensed ->
                _uiState.update {
                    it.copy(
                        isLicensed = licensed,
                        licenseKey = userPreferences.getLicenseKey(),
                        activationDate = userPreferences.getLicenseActivatedDate()
                    )
                }
            }
        }

        // Sync cloud status on launch
        syncCloudStatus()
    }

    fun syncCloudStatus() {
        viewModelScope.launch {
            licenseRepository.syncLicenseStatusFromCloud()
            _uiState.update {
                it.copy(
                    isLicensed = userPreferences.isLicensed(),
                    licenseKey = userPreferences.getLicenseKey(),
                    activationDate = userPreferences.getLicenseActivatedDate()
                )
            }
        }
    }

    fun activateLicense(inputKey: String) {
        val trimmed = inputKey.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "कृपया वैध license key दर्ज करें।") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            val result = licenseRepository.activateLicense(trimmed)
            when (result) {
                is LicenseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLicensed = true,
                            licenseKey = result.key,
                            activationDate = result.activationDate,
                            successMessage = result.message,
                            errorMessage = null
                        )
                    }
                }
                is LicenseResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            successMessage = null
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
