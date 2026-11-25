package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.features.alarms.R
import java.time.format.DateTimeFormatter

@Composable
fun AlarmCard(
    alarm: Alarm,
    is24HourFormat: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSkipped = alarm.skippedAt != null
    val isActive = alarm.isEnabled && !isSkipped

    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val timeContentColor = when {
        isActive -> MaterialTheme.colorScheme.onSurface
        isSkipped -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .then(if (!alarm.isQuickAlarm) Modifier.clickable(onClick = onClick) else Modifier),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (alarm.isEnabled) {
                        drawRect(
                            color = if (!isSkipped) {
                                primaryColor
                            } else {
                                secondaryColor
                            },
                            size = Size(4.dp.toPx(), size.height)
                        )
                    }
                }
                .padding(
                    start = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.extraSmall,
                    top = MaterialTheme.spacing.smallMedium,
                    bottom = MaterialTheme.spacing.smallMedium
                ),
        ) {
            // Top Row: Repeat Info or Quick Alarm Tag
            val skippedAt = alarm.skippedAt
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (alarm.isQuickAlarm) {
                    Icon(
                        imageVector = ReMindIcons.FlashOn,
                        contentDescription = null,
                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = stringResource(R.string.quick_alarm_tag),
                        style = LiteverTokens.typography.labelSmallEmphasized,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = getRepeatText(alarm),
                        style = LiteverTokens.typography.labelSmallEmphasized,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isSkipped && skippedAt != null) {
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                    Box(
                        modifier = Modifier
                            .size(MaterialTheme.spacing.extraSmall)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))

                    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
                    Text(
                        text = stringResource(R.string.skipped_next_format, skippedAt.format(dateFormatter)),
                        style = LiteverTokens.typography.labelSmallEmphasized,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Middle Row: Time + Controls
            val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(alarm.time, is24HourFormat)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFeatureSettings = "tnum"
                        ),
                        color = timeContentColor,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (amPm != null) {
                        Text(
                            text = amPm.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = timeContentColor.copy(alpha = 0.75f),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .padding(start = MaterialTheme.spacing.extraSmall)
                                .alignByBaseline()
                        )
                    }
                }

                // Mission Icons
                MissionIcons(
                    missions = alarm.missions,
                    modifier = Modifier.padding(start = MaterialTheme.spacing.small)
                )

                Spacer(Modifier.weight(1F))

                Switch(
                    checked = isActive,
                    onCheckedChange = onToggle
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = ReMindIcons.MoreVert,
                        contentDescription = stringResource(R.string.action_more),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(MaterialTheme.spacing.mediumLarge),
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.tiny))

            // Bottom Row: Label - Italics and softer alpha if empty
            val isLabelEmpty = alarm.label.isEmpty()
            val labelText = if (isLabelEmpty) stringResource(R.string.no_label) else alarm.label
            val labelAlpha = when {
                isLabelEmpty && isActive -> 0.55f
                isLabelEmpty -> 0.4f
                isActive -> 1f
                else -> 0.7f
            }
            Text(
                text = labelText,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = labelAlpha),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = if (isLabelEmpty) FontStyle.Italic else FontStyle.Normal,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun MissionIcons(missions: List<Mission>, modifier: Modifier = Modifier) {
    if (missions.isEmpty()) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (missions.size <= 2) {
            missions.forEach { mission ->
                Icon(
                    imageVector = getMissionIcon(mission.type),
                    contentDescription = null,
                    modifier = Modifier
                        .size(MaterialTheme.spacing.medium)
                        .padding(horizontal = MaterialTheme.spacing.tiny),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        } else {
            Icon(
                imageVector = getMissionIcon(missions[0].type),
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.spacing.medium),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = CircleShape
            ) {
                Text(
                    text = "+${missions.size - 1}",
                    style = LiteverTokens.typography.labelSmallEmphasized.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
internal fun getMissionIcon(type: MissionType): ImageVector {
    return when (type) {
        MissionType.TYPING -> ReMindIcons.Keyboard
        MissionType.MATH -> ReMindIcons.Calculate
        MissionType.MEMORY_FIND_COLOR_TILES -> ReMindIcons.GridView
        MissionType.SHAKE -> ReMindIcons.Vibration
        MissionType.BARCODE -> ReMindIcons.QrCodeScanner
        MissionType.RANDOM -> ReMindIcons.Refresh
    }
}

@Composable
private fun getRepeatText(alarm: Alarm): String {
    return getRepeatSummaryText(alarm.repeatDays, alarm.time, alarm.date)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AlarmCardPreview() {
    val alarm = Alarm(
        id = 1L,
        time = java.time.LocalTime.of(7, 30),
        isEnabled = true,
        label = "Morning Workout",
        repeatDays = listOf(java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.FRIDAY)
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            AlarmCard(
                alarm = alarm,
                is24HourFormat = false,
                onToggle = {},
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AlarmCardDisabledPreview() {
    val alarm = Alarm(
        id = 1L,
        time = java.time.LocalTime.of(8, 0),
        isEnabled = false,
        label = "Weekend Alarm",
        repeatDays = listOf(java.time.DayOfWeek.SATURDAY, java.time.DayOfWeek.SUNDAY)
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            AlarmCard(
                alarm = alarm,
                is24HourFormat = true,
                onToggle = {},
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AlarmCardSkippedPreview() {
    val alarm = Alarm(
        id = 1L,
        time = java.time.LocalTime.of(9, 15),
        isEnabled = true,
        label = "Office Meeting",
        repeatDays = listOf(java.time.DayOfWeek.MONDAY),
        skippedAt = java.time.LocalDateTime.now()
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            AlarmCard(
                alarm = alarm,
                is24HourFormat = false,
                onToggle = {},
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AlarmCardNoLabelPreview() {
    val alarm = Alarm(
        id = 1L,
        time = java.time.LocalTime.of(6, 0),
        isEnabled = true,
        label = "",
        repeatDays = listOf(java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY)
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            AlarmCard(
                alarm = alarm,
                is24HourFormat = false,
                onToggle = {},
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}
