package vn.io.litever.remind.features.reminder.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RingtoneItem(
    val title: String,
    val uri: String?,
    val isSelected: Boolean = false,
    val isPlaying: Boolean = false
)

data class RingtoneSelectionUiState(
    val ringtones: List<RingtoneItem> = emptyList(),
    val selectedUri: String? = null,
    val playingUri: String? = IDLE_URI,
    val isLoading: Boolean = true
) {
    companion object {
        const val IDLE_URI = "__IDLE__"
        const val DEFAULT_URI = "__DEFAULT__"
    }
}

@HiltViewModel
class RingtoneSelectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
    private val _uiState = MutableStateFlow(RingtoneSelectionUiState())
    val uiState: StateFlow<RingtoneSelectionUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    init {
        loadRingtones()
    }

    private fun loadRingtones() {
        viewModelScope.launch {
            val ringtoneList = mutableListOf<RingtoneItem>()
            
            // Add Default option
            ringtoneList.add(RingtoneItem("Default", null))

            val manager = RingtoneManager(context)
            manager.setType(RingtoneManager.TYPE_ALARM)
            val cursor = manager.cursor
            
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = manager.getRingtoneUri(cursor.position).toString()
                ringtoneList.add(RingtoneItem(title, uri))
            }
            
            _uiState.update { it.copy(ringtones = ringtoneList, isLoading = false) }
        }
    }

    fun selectRingtone(uri: String?) {
        val isSame = _uiState.value.selectedUri == uri
        _uiState.update { state ->
            state.copy(
                selectedUri = uri,
                ringtones = state.ringtones.map { it.copy(isSelected = it.uri == uri) }
            )
        }
        
        // If clicking the same one, toggle playback
        if (isSame) {
            togglePlayback(uri)
        } else {
            // New selection, start playback
            startPlayback(uri)
        }
    }

    private fun togglePlayback(uri: String?) {
        val internalUri = uri ?: RingtoneSelectionUiState.DEFAULT_URI
        if (_uiState.value.playingUri == internalUri) {
            stopPlayback()
        } else {
            startPlayback(uri)
        }
    }

    private fun startPlayback(uriString: String?) {
        stopPlayback()
        
        val uri = vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri(context, uriString)

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                
                // Use MEDIA volume for selection screen
                val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
                val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
                val volumeScale = currentVolume.toFloat() / maxVolume.toFloat()
                setVolume(volumeScale, volumeScale)
                
                prepareAsync()
                setOnPreparedListener { 
                    it.start()
                    val internalUri = uriString ?: RingtoneSelectionUiState.DEFAULT_URI
                    _uiState.update { state -> 
                        state.copy(
                            playingUri = internalUri,
                            ringtones = state.ringtones.map { item -> item.copy(isPlaying = item.uri == uriString) }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _uiState.update { state -> 
            state.copy(
                playingUri = RingtoneSelectionUiState.IDLE_URI,
                ringtones = state.ringtones.map { it.copy(isPlaying = false) }
            )
        }
    }

    fun setInitialSelection(uri: String?) {
        _uiState.update { state ->
            state.copy(
                selectedUri = uri,
                ringtones = state.ringtones.map { it.copy(isSelected = it.uri == uri) }
            )
        }
    }

    override fun onCleared() {
        stopPlayback()
        super.onCleared()
    }
}
