package vn.io.litever.remind.features.alarms.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.semanticColors
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.MissedAlarm
import vn.io.litever.remind.core.model.MissedReason
import vn.io.litever.remind.features.alarms.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MissedAlarmsScreen(
    missedAlarms: List<MissedAlarm>,
    is24HourFormat: Boolean,
    onDismiss: () -> Unit,
    onCheckPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onDismiss()
    }

    val hasSystemOrPermissionIssue = remember(missedAlarms) {
        missedAlarms.any {
            it.reason == MissedReason.PERMISSION_MISSING ||
                it.reason == MissedReason.DND_ACTIVE ||
                it.reason == MissedReason.UNKNOWN
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                        .padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.medium
                        ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    if (hasSystemOrPermissionIssue) {
                        FilledTonalButton(
                            onClick = onCheckPermissions,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Icon(
                                imageVector = ReMindIcons.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(
                                text = stringResource(R.string.missed_alarms_action_check_permissions),
                                style = LiteverTokens.typography.titleSmallEmphasized
                            )
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.missed_alarms_action_got_it),
                            style = LiteverTokens.typography.titleMediumEmphasized
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.large
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) {
            // Header
            item(key = "missed_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.small),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasSystemOrPermissionIssue) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.semanticColors.warningContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ReMindIcons.NotificationsPaused,
                            contentDescription = null,
                            tint = if (hasSystemOrPermissionIssue) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.semanticColors.onWarningContainer
                            },
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    Text(
                        text = stringResource(R.string.missed_alarms_dialog_title),
                        style = LiteverTokens.typography.headlineSmallEmphasized,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                    Text(
                        text = stringResource(R.string.missed_alarms_dialog_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Missed Alarms List
            items(
                items = missedAlarms,
                key = { it.id }
            ) { missed ->
                MissedAlarmCard(
                    missed = missed,
                    is24HourFormat = is24HourFormat
                )
            }
        }
    }
}

@Composable
private fun MissedAlarmCard(
    missed: MissedAlarm,
    is24HourFormat: Boolean
) {
    val currentLocale = LocalLocale.current.platformLocale
    val dateTime = remember(missed.scheduledTime) {
        val instant = Instant.ofEpochMilli(missed.scheduledTime)
        val zoneId = runCatching { ZoneId.systemDefault() }.getOrDefault(ZoneId.of("UTC"))
        LocalDateTime.ofInstant(instant, zoneId)
    }
    val dateFormatter = remember(currentLocale) {
        DateTimeFormatter.ofPattern("EEE, d MMM", currentLocale)
    }
    val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(dateTime.toLocalTime(), is24HourFormat)
    val dateText = dateTime.format(dateFormatter).replaceFirstChar { it.uppercase(currentLocale) }

    val icon: ImageVector
    val reasonText: String
    val pillContainerColor: Color
    val pillContentColor: Color

    when (missed.reason) {
        MissedReason.PERMISSION_MISSING -> {
            icon = ReMindIcons.Security
            reasonText = stringResource(R.string.missed_reason_permission)
            pillContainerColor = MaterialTheme.colorScheme.errorContainer
            pillContentColor = MaterialTheme.colorScheme.onErrorContainer
        }
        MissedReason.DND_ACTIVE -> {
            icon = ReMindIcons.DoNotDisturbOn
            reasonText = stringResource(R.string.missed_reason_dnd)
            pillContainerColor = MaterialTheme.colorScheme.errorContainer
            pillContentColor = MaterialTheme.colorScheme.onErrorContainer
        }
        MissedReason.UNKNOWN -> {
            icon = ReMindIcons.Warning
            reasonText = stringResource(R.string.missed_reason_unknown)
            pillContainerColor = MaterialTheme.semanticColors.warningContainer
            pillContentColor = MaterialTheme.semanticColors.onWarningContainer
        }
        MissedReason.POWER_OFF -> {
            icon = ReMindIcons.PowerOff
            reasonText = stringResource(R.string.missed_reason_power_off)
            pillContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            pillContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
        MissedReason.TIMEOUT -> {
            icon = ReMindIcons.NotificationsPaused
            reasonText = stringResource(R.string.missed_reason_timeout)
            pillContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            pillContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = timeStr,
                        style = LiteverTokens.typography.headlineSmallEmphasized.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (amPm != null) {
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(
                            text = amPm.uppercase(currentLocale),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (missed.alarmLabel.isNotBlank()) {
                Text(
                    text = missed.alarmLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = pillContainerColor,
                contentColor = pillContentColor,
                shape = CircleShape
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = reasonText,
                        style = LiteverTokens.typography.labelSmallEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Missed Alarms Screen - Light")
@Composable
private fun MissedAlarmsScreenPreview() {
    ReMindTheme {
        MissedAlarmsScreen(
            missedAlarms = listOf(
                MissedAlarm(
                    id = 1,
                    alarmId = 101,
                    alarmLabel = "Morning Yoga",
                    scheduledTime = System.currentTimeMillis() - 3_600_000L,
                    reason = MissedReason.POWER_OFF
                ),
                MissedAlarm(
                    id = 2,
                    alarmId = 102,
                    alarmLabel = "Take Medicine",
                    scheduledTime = System.currentTimeMillis() - 7_200_000L,
                    reason = MissedReason.PERMISSION_MISSING
                ),
                MissedAlarm(
                    id = 3,
                    alarmId = 103,
                    alarmLabel = "",
                    scheduledTime = System.currentTimeMillis() - 10_800_000L,
                    reason = MissedReason.TIMEOUT
                )
            ),
            is24HourFormat = false,
            onDismiss = {},
            onCheckPermissions = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Missed Alarms Screen - Dark")
@Composable
private fun MissedAlarmsScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        MissedAlarmsScreen(
            missedAlarms = listOf(
                MissedAlarm(
                    id = 1,
                    alarmId = 101,
                    alarmLabel = "Quick Nap",
                    scheduledTime = System.currentTimeMillis() - 1_800_000L,
                    reason = MissedReason.UNKNOWN
                )
            ),
            is24HourFormat = true,
            onDismiss = {},
            onCheckPermissions = {}
        )
    }
}
