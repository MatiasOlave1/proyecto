package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao
import com.camposocampoolavevargas.proyecto.data.local.entity.SleepRecordEntity
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class SleepBarData(
    val day: String,
    val hours: Float
)

@HiltViewModel
class SleepHistoryViewModel @Inject constructor(
    private val sleepRecordDao: SleepRecordDao,
    private val userSession: UserSession
) : BaseViewModel() {

    private val _records =
        MutableStateFlow<List<SleepRecordEntity>>(emptyList())
    val records: StateFlow<List<SleepRecordEntity>> = _records

    private val _chartData =
        MutableStateFlow<List<SleepBarData>>(emptyList())
    val chartData: StateFlow<List<SleepBarData>> = _chartData

    init {
        loadRecords()
    }

    /** Fuerza recarga de registros — invocable desde LaunchedEffect en la UI. */
    fun reload() {
        loadRecords()
    }

    private fun loadRecords() {
        val userId = userSession.getActiveUserId() ?: return

        viewModelScope.launch {
            // Obtener registros ordenados DESC por fecha (más recientes primero)
            val registros = sleepRecordDao
                .getRecordsByUserIdDirect(userId)
                .sortedByDescending { it.date }

            _records.value = registros

            _chartData.value =
                registros
                    .take(7)
                    .reversed()
                    .map {
                        val fecha = LocalDate.parse(it.date)
                        SleepBarData(
                            day = fecha.format(DateTimeFormatter.ofPattern("dd")),
                            hours = it.durationMinutes / 60f
                        )
                    }
        }
    }
}