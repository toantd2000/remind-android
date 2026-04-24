package vn.io.litever.remind.features.settings.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vn.io.litever.remind.core.common.util.PermissionChecker
import javax.inject.Inject

data class PermissionUiState(
    val isNotificationGranted: Boolean = false,
    val isExactAlarmGranted: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val isBatteryOptIgnored: Boolean = false
)

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionChecker: vn.io.litever.remind.core.common.util.PermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PermissionUiState(
            isNotificationGranted = permissionChecker.hasNotificationPermission(),
            isExactAlarmGranted = permissionChecker.hasExactAlarmPermission(),
            isOverlayGranted = permissionChecker.hasOverlayPermission(),
            isBatteryOptIgnored = permissionChecker.isIgnoringBatteryOptimizations()
        )
    )
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    fun refreshPermissions() {
        _uiState.value = PermissionUiState(
            isNotificationGranted = permissionChecker.hasNotificationPermission(),
            isExactAlarmGranted = permissionChecker.hasExactAlarmPermission(),
            isOverlayGranted = permissionChecker.hasOverlayPermission(),
            isBatteryOptIgnored = permissionChecker.isIgnoringBatteryOptimizations()
        )
    }
}










