package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CommandEntity
import com.example.data.repository.CommandHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val commandHistoryRepository: CommandHistoryRepository
) : ViewModel() {

    val historyItems: StateFlow<List<CommandEntity>> = commandHistoryRepository.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteCommand(id: Long) {
        viewModelScope.launch {
            commandHistoryRepository.deleteCommand(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            commandHistoryRepository.clearHistory()
        }
    }
}
