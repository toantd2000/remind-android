package vn.io.litever.remind.features.alarms.ui

import androidx.compose.runtime.Composable
import vn.io.litever.remind.core.designsystem.components.snooze.SnoozeOptionRow
import vn.io.litever.remind.core.designsystem.components.snooze.SnoozeSettingsRoute as SharedSnoozeSettingsRoute
import vn.io.litever.remind.core.designsystem.components.snooze.SnoozeSettingsScreen as SharedSnoozeSettingsScreen

@Composable
fun SnoozeSettingsRoute(
    initialEnabled: Boolean,
    initialInterval: Int,
    initialRepeatCount: Int,
    onBackClick: () -> Unit,
    onSave: (Boolean, Int, Int) -> Unit
) {
    SharedSnoozeSettingsRoute(
        initialEnabled = initialEnabled,
        initialInterval = initialInterval,
        initialRepeatCount = initialRepeatCount,
        onBackClick = onBackClick,
        onSave = onSave
    )
}

@Composable
fun SnoozeSettingsScreen(
    enabled: Boolean,
    interval: Int,
    repeatCount: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onRepeatCountChange: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    SharedSnoozeSettingsScreen(
        enabled = enabled,
        interval = interval,
        repeatCount = repeatCount,
        onEnabledChange = onEnabledChange,
        onIntervalChange = onIntervalChange,
        onRepeatCountChange = onRepeatCountChange,
        onBackClick = onBackClick
    )
}











