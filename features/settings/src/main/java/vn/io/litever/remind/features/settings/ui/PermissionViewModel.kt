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
    val isFullScreenIntentGranted: Boolean = true,
    val isOverlayGranted: Boolean = false,
    val isBatteryOptIgnored: Boolean = false,
    val isDndPolicyGranted: Boolean = false
)

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow(readCurrentPermissions())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    fun refreshPermissions() {
        _uiState.value = readCurrentPermissions()
    }

    private fun readCurrentPermissions(): PermissionUiState = PermissionUiState(
        isNotificationGranted = permissionChecker.hasNotificationPermission(),
        isExactAlarmGranted = permissionChecker.hasExactAlarmPermission(),
        isFullScreenIntentGranted = permissionChecker.hasFullScreenIntentPermission(),
        isOverlayGranted = permissionChecker.hasOverlayPermission(),
        isBatteryOptIgnored = permissionChecker.isIgnoringBatteryOptimizations(),
        isDndPolicyGranted = permissionChecker.hasNotificationPolicyAccess()
    )
}
