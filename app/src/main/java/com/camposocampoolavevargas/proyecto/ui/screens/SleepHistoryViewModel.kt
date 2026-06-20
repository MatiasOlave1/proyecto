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
import javax.inject.Inject

@HiltViewModel
class SleepHistoryViewModel @Inject constructor(
    private val sleepRecordDao: SleepRecordDao,
    private val userSession: UserSession
) : BaseViewModel() {

    private val _records =
        MutableStateFlow<List<SleepRecordEntity>>(emptyList())

    val records: StateFlow<List<SleepRecordEntity>> = _records

    init {
        loadRecords()
    }

    private fun loadRecords() {
        val userId = userSession.getActiveUserId() ?: return

        viewModelScope.launch {
            _records.value =
                sleepRecordDao.getRecordsByUserIdDirect(userId)
        }
    }
}