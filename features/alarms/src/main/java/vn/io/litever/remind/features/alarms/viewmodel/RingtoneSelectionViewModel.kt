package vn.io.litever.remind.features.alarms.viewmodel

import android.content.Context
import android.media.RingtoneManager
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import vn.io.litever.remind.core.common.audio.AudioPlayer
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
    @ApplicationContext private val context: Context,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(RingtoneSelectionUiState())
    val uiState: StateFlow<RingtoneSelectionUiState> = _uiState.asStateFlow()

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

        audioPlayer.play(
            uri = uri,
            usage = android.media.AudioAttributes.USAGE_MEDIA,
            contentType = android.media.AudioAttributes.CONTENT_TYPE_MUSIC,
            loop = true
        )

        val internalUri = uriString ?: RingtoneSelectionUiState.DEFAULT_URI
        _uiState.update { state -> 
            state.copy(
                playingUri = internalUri,
                ringtones = state.ringtones.map { item -> item.copy(isPlaying = item.uri == uriString) }
            )
        }
    }

    private fun stopPlayback() {
        audioPlayer.stop()
        _uiState.update { state -> 
            state.copy(
                playingUri = RingtoneSelectionUiState.IDLE_URI,
                ringtones = state.ringtones.map { it.copy(isPlaying = false) }
            )
        }
    }

    fun setInitialSelection(uri: String?) {
        _uiState.update { state ->
            val ringtones = state.ringtones.toMutableList()
            if (!uri.isNullOrEmpty() && ringtones.none { it.uri == uri }) {
                // If the initial URI is a custom one not in the system list, add it
                val title = getFileName(Uri.parse(uri)) ?: "Custom Ringtone"
                ringtones.add(1, RingtoneItem(title, uri))
            }
            state.copy(
                selectedUri = uri,
                ringtones = ringtones.map { it.copy(isSelected = it.uri == uri) }
            )
        }
    }

    fun addCustomRingtone(uri: Uri) {
        viewModelScope.launch {
            // Grant persistable permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Not always possible depending on picker and SDK version
            }

            val title = getFileName(uri) ?: "Custom Ringtone"
            val uriString = uri.toString()
            
            _uiState.update { state ->
                val newList = state.ringtones.toMutableList()
                if (newList.none { it.uri == uriString }) {
                    newList.add(1, RingtoneItem(title, uriString)) // Add after Default
                }
                state.copy(ringtones = newList)
            }
            selectRingtone(uriString)
        }
    }

    private fun getFileName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        stopPlayback()
        super.onCleared()
    }
}










