package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao
import com.camposocampoolavevargas.proyecto.data.local.entity.SleepRecordEntity
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class HistoryFilter(val displayName: String) {
    LAST_7_DAYS("Últimos 7 días"),
    LAST_30_DAYS("Últimos 30 días"),
    ALL("Todos los registros")
}

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

    private val _selectedFilter = MutableStateFlow(HistoryFilter.LAST_7_DAYS)
    val selectedFilter: StateFlow<HistoryFilter> = _selectedFilter

    val filteredRecords: StateFlow<List<SleepRecordEntity>> = combine(
        _records,
        _selectedFilter
    ) { recordsList, filter ->
        when (filter) {
            HistoryFilter.LAST_7_DAYS -> {
                val limitDate = LocalDate.now().minusDays(6)
                recordsList.filter {
                    try {
                        val recordDate = LocalDate.parse(it.date)
                        !recordDate.isBefore(limitDate)
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            HistoryFilter.LAST_30_DAYS -> {
                val limitDate = LocalDate.now().minusDays(29)
                recordsList.filter {
                    try {
                        val recordDate = LocalDate.parse(it.date)
                        !recordDate.isBefore(limitDate)
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            HistoryFilter.ALL -> recordsList
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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