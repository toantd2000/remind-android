package vn.io.litever.remind.core.ads.api

sealed class AdState {
    data object Idle : AdState()
    data object Loading : AdState()
    data object Loaded : AdState()
    data class Failed(val error: String) : AdState()
}
