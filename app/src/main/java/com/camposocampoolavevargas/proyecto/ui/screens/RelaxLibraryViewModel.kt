package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.repository.DisconnectSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RelaxLibraryUiState(
    val reminderOffsetMinutes: Int = 90
)

@HiltViewModel
class RelaxLibraryViewModel @Inject constructor(
    private val settingsRepository: DisconnectSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RelaxLibraryUiState())
    val uiState: StateFlow<RelaxLibraryUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val offset = settingsRepository.getReminderOffsetMinutes()
            _uiState.value = RelaxLibraryUiState(reminderOffsetMinutes = offset)
        }
    }
}
