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

import kotlinx.coroutines.flow.map

enum class HistoryFilter(val displayName: String) {
    LAST_7_DAYS("Últimos 7 días"),
    LAST_30_DAYS("Últimos 30 días"),
    ALL("Todos los registros")
}

data class SleepBarData(
    val day: String,
    val hours: Float,
    val quality: com.camposocampoolavevargas.proyecto.data.local.model.SleepQuality?,
    val date: String
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

    private val _currentWeekOffset = MutableStateFlow(0)
    val currentWeekOffset: StateFlow<Int> = _currentWeekOffset

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

    val chartData: StateFlow<List<SleepBarData>> = combine(
        _records,
        _currentWeekOffset
    ) { recordsList, offset ->
        val today = LocalDate.now()
        val monday = today.plusWeeks(offset.toLong()).with(java.time.DayOfWeek.MONDAY)
        val initials = listOf("L", "M", "X", "J", "V", "S", "D")
        
        (0..6).map { i ->
            val date = monday.plusDays(i.toLong())
            val dateStr = date.toString()
            val record = recordsList.find { it.date == dateStr }
            SleepBarData(
                day = initials[i],
                hours = record?.let { it.durationMinutes / 60f } ?: 0f,
                quality = record?.quality,
                date = dateStr
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val weekRangeText: StateFlow<String> = _currentWeekOffset.map { offset ->
        val today = LocalDate.now()
        val monday = today.plusWeeks(offset.toLong()).with(java.time.DayOfWeek.MONDAY)
        val sunday = monday.plusDays(6)
        val formatter = DateTimeFormatter.ofPattern("d 'de' MMM", java.util.Locale("es", "ES"))
        val formatterWithYear = DateTimeFormatter.ofPattern("d 'de' MMM yyyy", java.util.Locale("es", "ES"))
        
        if (monday.year == sunday.year) {
            "${monday.format(formatter)} - ${sunday.format(formatterWithYear)}".uppercase()
        } else {
            "${monday.format(formatterWithYear)} - ${sunday.format(formatterWithYear)}".uppercase()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    init {
        loadRecords()
    }

    /** Fuerza recarga de registros — invocable desde LaunchedEffect en la UI. */
    fun reload() {
        loadRecords()
    }

    fun selectPreviousWeek() {
        _currentWeekOffset.value -= 1
    }

    fun selectNextWeek() {
        if (_currentWeekOffset.value < 0) {
            _currentWeekOffset.value += 1
        }
    }

    private fun loadRecords() {
        val userId = userSession.getActiveUserId() ?: return

        viewModelScope.launch {
            // Obtener registros ordenados DESC por fecha (más recientes primero)
            val registros = sleepRecordDao
                .getRecordsByUserIdDirect(userId)
                .sortedByDescending { it.date }

            _records.value = registros
        }
    }
}