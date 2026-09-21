package vn.io.litever.remind.features.today.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.NextAlarmUiState
import vn.io.litever.remind.features.today.R
import java.time.LocalTime as JavaLocalTime

/**
 * Hiển thị trạng thái báo thức tiếp theo trên TodayScreen.
 * Vùng này CHỈ để hiển thị trạng thái, KHÔNG THỂ tương tác (non-interactive).
 *
 * Bố cục sắp xếp:
 * - CỘT 1: ICON/ẢNH BÁO THỨC Ô VUÔNG
 * - CỘT 2:
 *     - Hàng 1: Text "Báo thức tiếp theo" (kèm badge đếm ngược thời gian còn lại)
 *     - Hàng 2: Giờ báo thức -> Cột nhiệm vụ báo thức | Cột ngày lặp lại
 *     - Hàng 3: Nhãn báo thức
 */
@Composable
fun TodayNextAlarmView(
    state: NextAlarmUiState,
    is24HourFormat: Boolean = false,
    modifier: Modifier = Modifier
) {
    when (state) {
        is NextAlarmUiState.Remaining -> {
            val alarm = state.alarm

            Surface(
                modifier = modifier.fillMaxWidth(),
                shape = LiteverTheme.shapes.large,
                color = LiteverTheme.colors.surfaceContainer,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LiteverTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
                ) {
                    // CỘT 1: ICON/ẢNH BÁO THỨC Ô VUÔNG
                    Card(
                        modifier = Modifier.size(76.dp),
                        shape = LiteverTheme.shapes.large
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .background(LiteverTheme.colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp),
                                tint = LiteverTheme.colors.primary
                            )
                        }
                    }

                    // CỘT 2: THÔNG TIN BÁO THỨC
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Hàng 1: Text báo thức tiếp theo trên cùng (kèm badge đếm ngược còn lại)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.next_alarm_title),
                                style = LiteverTheme.typography.labelSmall,
                                color = LiteverTheme.colors.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = LiteverTheme.spacing.small)
                            )

                            Surface(
                                shape = LiteverTheme.shapes.small,
                                color = LiteverTheme.colors.tertiaryContainer,
                                contentColor = LiteverTheme.colors.onTertiaryContainer
                            ) {
                                Text(
                                    text = formatNextAlarmRemainingText(state),
                                    style = LiteverTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Hàng 2: Giờ báo thức -> Cột icon nhiệm vụ | Cột ngày lặp lại
                        val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(alarm.time, is24HourFormat)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Giờ báo thức
                            Text(
                                text = timeStr,
                                style = LiteverTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = LiteverTheme.colors.onSurface
                            )
                            if (amPm != null) {
                                Text(
                                    text = amPm.uppercase(),
                                    style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LiteverTheme.colors.onSurface,
                                    modifier = Modifier
                                        .padding(start = 2.dp, bottom = 2.dp)
                                        .align(Alignment.Bottom)
                                )
                            }

                            // Cột icon nhiệm vụ (nếu có)
                            if (alarm.missions.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                                TodayMissionIcons(
                                    missions = alarm.missions
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Cột ngày lặp lại
                            Text(
                                text = getRepeatSummaryText(alarm.repeatDays, alarm.time, alarm.date),
                                style = LiteverTheme.typography.labelSmall,
                                color = LiteverTheme.colors.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Hàng 3: Nhãn báo thức
                        val labelText = alarm.label.ifEmpty { stringResource(R.string.no_label) }
                        Text(
                            text = labelText,
                            color = LiteverTheme.colors.onSurfaceVariant,
                            style = LiteverTheme.typography.bodySmall.copy(
                                fontStyle = if (alarm.label.isEmpty()) FontStyle.Italic else FontStyle.Normal,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        NextAlarmUiState.AllOff -> {
            val isDark = !LiteverTheme.colors.isLight
            val illustrationRes = if (isDark) {
                vn.io.litever.remind.core.designsystem.R.drawable.no_alarm_illustration_dark
            } else {
                vn.io.litever.remind.core.designsystem.R.drawable.no_alarm_illustration
            }

            Surface(
                modifier = modifier.fillMaxWidth(),
                color = LiteverTheme.colors.surfaceContainerLow,
                shape = LiteverTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LiteverTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
                ) {
                    Card(
                        modifier = Modifier.size(76.dp),
                        shape = LiteverTheme.shapes.medium
                    ) {
                        Image(
                            painter = painterResource(illustrationRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.no_upcoming_alarm_title),
                            style = LiteverTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LiteverTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.no_upcoming_alarm_desc),
                            style = LiteverTheme.typography.bodySmall,
                            color = LiteverTheme.colors.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayMissionIcons(missions: List<Mission>, modifier: Modifier = Modifier) {
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
                        .size(LiteverTheme.spacing.medium)
                        .padding(horizontal = 1.dp),
                    tint = LiteverTheme.colors.primary.copy(alpha = 0.8f)
                )
            }
        } else {
            Icon(
                imageVector = getMissionIcon(missions[0].type),
                contentDescription = null,
                modifier = Modifier.size(LiteverTheme.spacing.medium),
                tint = LiteverTheme.colors.primary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Surface(
                color = LiteverTheme.colors.primaryContainer.copy(alpha = 0.8f),
                shape = CircleShape
            ) {
                Text(
                    text = "+${missions.size - 1}",
                    style = LiteverTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = LiteverTheme.colors.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getMissionIcon(type: MissionType): ImageVector {
    return when (type) {
        MissionType.TYPING -> Icons.Rounded.Keyboard
        MissionType.MATH -> Icons.Rounded.Calculate
        MissionType.QR_CODE -> Icons.Rounded.QrCodeScanner
        MissionType.SHAKE -> Icons.Rounded.Smartphone
        MissionType.STEP -> Icons.AutoMirrored.Rounded.DirectionsWalk
        MissionType.COLOR_MATCH -> Icons.Rounded.Palette
        MissionType.TAP_CHALLENGE -> Icons.Rounded.TouchApp
        MissionType.FIND_ITEM -> Icons.Rounded.Search
        MissionType.MEMORY_FIND_COLOR_TILES -> Icons.Rounded.GridView
    }
}

@Composable
private fun getRepeatSummaryText(
    repeatDays: List<DayOfWeek>,
    time: JavaLocalTime,
    date: java.time.LocalDate? = null
): String {
    if (date != null) {
        val formatter = java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
        return stringResource(R.string.one_time_on, date.format(formatter))
    }

    if (repeatDays.isEmpty()) {
        val now = JavaLocalTime.now()
        return if (time.isAfter(now)) {
            stringResource(R.string.today)
        } else {
            stringResource(R.string.tomorrow)
        }
    }

    val isDaily = repeatDays.distinct().size == 7
    if (isDaily) {
        return stringResource(R.string.every_day)
    }

    val dayNames = mapOf(
        DayOfWeek.MONDAY to stringResource(R.string.day_mon),
        DayOfWeek.TUESDAY to stringResource(R.string.day_tue),
        DayOfWeek.WEDNESDAY to stringResource(R.string.day_wed),
        DayOfWeek.THURSDAY to stringResource(R.string.day_thu),
        DayOfWeek.FRIDAY to stringResource(R.string.day_fri),
        DayOfWeek.SATURDAY to stringResource(R.string.day_sat),
        DayOfWeek.SUNDAY to stringResource(R.string.day_sun)
    )

    return repeatDays.distinct().sortedBy { it.ordinal }.joinToString(", ") { day ->
        dayNames[day] ?: ""
    }
}

@Composable
private fun formatNextAlarmRemainingText(state: NextAlarmUiState.Remaining): String {
    return when {
        state.days > 0 -> stringResource(R.string.remaining_short_days_hours, state.days, state.hours)
        state.hours > 0 -> stringResource(R.string.remaining_short_hours_mins, state.hours, state.minutes)
        state.minutes > 0 -> stringResource(R.string.remaining_short_mins, state.minutes)
        else -> stringResource(R.string.remaining_short_less_minute)
    }
}

@Preview(showBackground = true)
@Composable
fun TodayNextAlarmViewRemainingPreview() {
    val mockAlarm = Alarm(
        id = 1L,
        time = JavaLocalTime.of(7, 30),
        isEnabled = true,
        label = "Thức dậy đón bình minh",
        repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
        missions = listOf(
            Mission(id = 1, alarmId = 1, type = MissionType.TYPING, order = 1),
            Mission(id = 2, alarmId = 1, type = MissionType.MATH, order = 2)
        )
    )

    ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TodayNextAlarmView(
                state = NextAlarmUiState.Remaining(
                    days = 0,
                    hours = 6,
                    minutes = 45,
                    alarm = mockAlarm
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodayNextAlarmViewAllOffPreview() {
    ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TodayNextAlarmView(
                state = NextAlarmUiState.AllOff
            )
        }
    }
}
