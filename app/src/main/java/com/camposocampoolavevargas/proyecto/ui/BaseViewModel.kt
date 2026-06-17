package com.camposocampoolavevargas.proyecto.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel providing standard Coroutine scopes and UI state helper routines.
 */
open class BaseViewModel : ViewModel() {

    protected val scope = viewModelScope

    /**
     * Represents the current state of a generic operation.
     */
    sealed class UiState<out T> {
        object Loading : UiState<Nothing>()
        data class Success<out T>(val data: T) : UiState<T>()
        data class Error(val message: String) : UiState<Nothing>()
    }

    /**
     * Executes a suspend block within the viewModelScope, updating the provided
     * [MutableStateFlow] with [UiState.Loading], [UiState.Success], or [UiState.Error].
     */
    protected fun <T> handleResult(
        stateFlow: MutableStateFlow<UiState<T>>,
        block: suspend () -> T
    ) {
        scope.launch {
            stateFlow.value = UiState.Loading
            try {
                val result = block()
                stateFlow.value = UiState.Success(result)
            } catch (e: Exception) {
                stateFlow.value = UiState.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }
    }
}

