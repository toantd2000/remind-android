package vn.io.litever.remind.core.designsystem.components.ringtone

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
