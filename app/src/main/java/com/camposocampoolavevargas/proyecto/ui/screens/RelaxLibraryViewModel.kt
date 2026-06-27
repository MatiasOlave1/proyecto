package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.repository.DisconnectSettingsRepository
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RelaxLibraryUiState(
    val reminderOffsetMinutes: Int = 90,
    val spotifyTrackUri: String? = null,
    val spotifyTrackName: String? = null,
    val alarmSet: Boolean = false,
    val alarmHour: Int = 7,
    val alarmMinute: Int = 0
)

data class SpotifyTrack(val name: String, val artist: String, val uri: String)

@HiltViewModel
class RelaxLibraryViewModel @Inject constructor(
    private val settingsRepository: DisconnectSettingsRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(RelaxLibraryUiState())
    val uiState: StateFlow<RelaxLibraryUiState> = _uiState.asStateFlow()

    val curatedTracks = listOf(
        SpotifyTrack("Guitarra Acústica Matutina", "Acoustic Morning", "spotify:track:6rqhFz57hlTky3649E58N6"),
        SpotifyTrack("Alarma Lofi Relajante", "Lofi Awakening", "spotify:track:0VjIjW4GlUZ372z176kH2x"),
        SpotifyTrack("Sonidos de Lluvia Suave", "Nature Ambient", "spotify:track:7H62T3L0pUaUj8lCjD5wGf"),
        SpotifyTrack("Despertar con Meditación", "Ambient Flow", "spotify:track:17v087S6ScyyS04E0Z6zGq"),
        SpotifyTrack("Ingresar canción personalizada...", "Pega un enlace", "custom"),
        SpotifyTrack("Solo Alarma Local (Sin Spotify)", "Interno", "")
    )

    fun saveCustomSpotifyTrack(urlOrUri: String) {
        val cleanUri = parseSpotifyUri(urlOrUri)
        val name = "Canción Personalizada"
        selectSpotifyTrack(cleanUri, name)
    }

    private fun parseSpotifyUri(input: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith("spotify:")) {
            return trimmed
        }
        val regex = Regex("/track/([^?/]+)")
        val match = regex.find(trimmed)
        return if (match != null) {
            "spotify:track:${match.groupValues[1]}"
        } else {
            trimmed
        }
    }

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            val offset = settingsRepository.getReminderOffsetMinutes()
            val uri = userSession.getSpotifyAlarmUri()
            val name = userSession.getSpotifyAlarmName()
            val isSet = userSession.getAlarmSet()
            val hour = userSession.getAlarmHour()
            val minute = userSession.getAlarmMinute()
            _uiState.value = RelaxLibraryUiState(
                reminderOffsetMinutes = offset,
                spotifyTrackUri = uri,
                spotifyTrackName = name,
                alarmSet = isSet,
                alarmHour = hour,
                alarmMinute = minute
            )
        }
    }

    fun selectSpotifyTrack(uri: String?, name: String?) {
        userSession.saveSpotifyAlarm(uri, name)
        _uiState.value = _uiState.value.copy(
            spotifyTrackUri = uri,
            spotifyTrackName = name
        )
    }
}
