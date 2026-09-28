package vn.io.litever.remind.features.reminders.ui.components

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
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
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReminderTriggerSection(
    uiState: ReminderEditUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReminderTriggerSection(
        triggerType = uiState.triggerType,
        time = uiState.time,
        date = uiState.date,
        repeatDays = uiState.repeatDays,
        repeatInterval = uiState.repeatInterval,
        monthlyDays = uiState.monthlyDays,
        yearlyDates = uiState.yearlyDates,
        locationName = uiState.locationName,
        latitude = uiState.latitude,
        longitude = uiState.longitude,
        radiusMeters = uiState.radiusMeters,
        geofenceTransitionType = uiState.geofenceTransitionType,
        locationTimeWindows = uiState.locationTimeWindows.ifEmpty {
            if (uiState.locationTimeWindow != null) listOf(uiState.locationTimeWindow) else emptyList()
        },
        wifiSsid = uiState.wifiSsid,
        wifiTriggerType = uiState.wifiTriggerType,
        wifiTimeWindows = uiState.wifiTimeWindows.ifEmpty {
            if (uiState.wifiTimeWindow != null) listOf(uiState.wifiTimeWindow) else emptyList()
        },
        is24HourFormat = uiState.is24HourFormat,
        showLocationError = uiState.showLocationError,
        showWifiError = uiState.showWifiError,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun ReminderTriggerSection(
    triggerType: ReminderTriggerType,
    time: LocalTime,
    date: LocalDate?,
    repeatDays: List<DayOfWeek>,
    repeatInterval: ReminderRepeatInterval,
    monthlyDays: List<Int>,
    yearlyDates: List<ReminderMonthDay>,
    locationName: String,
    latitude: Double,
    longitude: Double,
    radiusMeters: Float,
    geofenceTransitionType: GeofenceTransitionType,
    locationTimeWindows: List<ReminderTimeWindow>,
    wifiSsid: String,
    wifiTriggerType: WifiTriggerType,
    wifiTimeWindows: List<ReminderTimeWindow>,
    is24HourFormat: Boolean,
    showLocationError: Boolean = false,
    showWifiError: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLocationEmpty = locationName.isBlank() && latitude == 0.0 && longitude == 0.0
    val hasLocationError = showLocationError && isLocationEmpty
    val hasWifiError = showWifiError && wifiSsid.isBlank()
    val hasError = (triggerType == ReminderTriggerType.LOCATION && hasLocationError) ||
            (triggerType == ReminderTriggerType.WIFI && hasWifiError)

    val (triggerTitleRes: Int, triggerIcon: ImageVector) = when (triggerType) {
        ReminderTriggerType.TIME -> Pair(R.string.trigger_time, ReMindIcons.AccessTime)
        ReminderTriggerType.LOCATION -> Pair(R.string.trigger_location, ReMindIcons.LocationOn)
        ReminderTriggerType.WIFI -> Pair(R.string.trigger_wifi, ReMindIcons.Wifi)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (hasError) {
            LiteverTheme.colors.errorContainer.copy(alpha = 0.15f)
        } else {
            LiteverTheme.colors.surfaceContainerLow
        },
        shape = LiteverTheme.shapes.large,
        border = if (hasError) BorderStroke(
            width = 1.dp,
            color = LiteverTheme.colors.error
        ) else null,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.medium)
        ) {
            // Header Row: Section label and trigger type pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.section_trigger_type),
                    style = LiteverTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiteverTheme.colors.onSurface
                )

                // Type Badge
                Surface(
                    shape = CircleShape,
                    color = LiteverTheme.colors.tertiaryContainer,
                    contentColor = LiteverTheme.colors.onTertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = LiteverTheme.spacing.small,
                            vertical = LiteverTheme.spacing.extraSmall
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
                    ) {
                        Icon(
                            imageVector = triggerIcon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(triggerTitleRes),
                            style = LiteverTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))

            // Body: Icon + Content + Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
            ) {
                // Leading Icon Container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (hasError) {
                                LiteverTheme.colors.error.copy(alpha = 0.15f)
                            } else {
                                LiteverTheme.colors.primaryContainer
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasError) ReMindIcons.ErrorOutline else triggerIcon,
                        contentDescription = null,
                        tint = if (hasError) LiteverTheme.colors.error else LiteverTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Detail Column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    when (triggerType) {
                        ReminderTriggerType.TIME -> {
                            TimeTriggerSummary(
                                time = time,
                                date = date,
                                repeatDays = repeatDays,
                                repeatInterval = repeatInterval,
                                monthlyDays = monthlyDays,
                                yearlyDates = yearlyDates,
                                is24HourFormat = is24HourFormat
                            )
                        }
                        ReminderTriggerType.LOCATION -> {
                            LocationTriggerSummary(
                                locationName = locationName,
                                latitude = latitude,
                                longitude = longitude,
                                radiusMeters = radiusMeters,
                                transitionType = geofenceTransitionType,
                                timeWindows = locationTimeWindows,
                                is24HourFormat = is24HourFormat,
                                hasError = hasLocationError
                            )
                        }
                        ReminderTriggerType.WIFI -> {
                            WifiTriggerSummary(
                                ssid = wifiSsid,
                                triggerType = wifiTriggerType,
                                timeWindows = wifiTimeWindows,
                                is24HourFormat = is24HourFormat,
                                hasError = hasWifiError
                            )
                        }
                    }
                }

                Icon(
                    imageVector = ReMindIcons.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.tap_to_edit_trigger),
                    tint = LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

            // Footer hint: Tap to edit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = ReMindIcons.Edit,
                    contentDescription = null,
                    tint = LiteverTheme.colors.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = stringResource(R.string.tap_to_edit_trigger),
                    style = LiteverTheme.typography.labelSmall,
                    color = LiteverTheme.colors.primary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TimeTriggerSummary(
    time: LocalTime,
    date: LocalDate?,
    repeatDays: List<DayOfWeek>,
    repeatInterval: ReminderRepeatInterval,
    monthlyDays: List<Int>,
    yearlyDates: List<ReminderMonthDay>,
    is24HourFormat: Boolean
) {
    val formattedTime = TimeFormatUtils.formatTime(time, is24HourFormat)

    Text(
        text = formattedTime,
        style = LiteverTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = LiteverTheme.colors.onSurface
    )

    Spacer(modifier = Modifier.height(2.dp))

    val context = LocalContext.current
    val repeatText = when (repeatInterval) {
        ReminderRepeatInterval.ONCE -> {
            if (date != null) {
                val dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                stringResource(R.string.repeat_interval_once) + " • " + dateStr
            } else {
                stringResource(R.string.repeat_interval_once)
            }
        }
        ReminderRepeatInterval.WEEKLY -> {
            if (repeatDays.size == 7) {
                stringResource(R.string.repeat_interval_daily)
            } else if (repeatDays.isEmpty()) {
                stringResource(R.string.repeat_interval_weekly)
            } else {
                val daysStr = repeatDays.joinToString(", ") { formatDayOfWeek(context, it) }
                stringResource(R.string.repeat_interval_weekly) + " • " + daysStr
            }
        }
        ReminderRepeatInterval.MONTHLY -> {
            if (monthlyDays.isEmpty()) {
                stringResource(R.string.repeat_interval_monthly)
            } else {
                val daysStr = monthlyDays.joinToString(", ")
                stringResource(R.string.repeat_interval_monthly) + " (Ngày $daysStr)"
            }
        }
        ReminderRepeatInterval.YEARLY -> {
            if (yearlyDates.isEmpty()) {
                stringResource(R.string.repeat_interval_yearly)
            } else {
                val datesStr = yearlyDates.joinToString(", ") { "${it.day}/${it.month}" }
                stringResource(R.string.repeat_interval_yearly) + " ($datesStr)"
            }
        }
    }

    Text(
        text = repeatText,
        style = LiteverTheme.typography.bodySmall,
        color = LiteverTheme.colors.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun LocationTriggerSummary(
    locationName: String,
    latitude: Double,
    longitude: Double,
    radiusMeters: Float,
    transitionType: GeofenceTransitionType,
    timeWindows: List<ReminderTimeWindow>,
    is24HourFormat: Boolean,
    hasError: Boolean
) {
    val headline = when {
        locationName.isNotBlank() -> locationName
        latitude != 0.0 || longitude != 0.0 -> String.format(Locale.US, "(%.4f, %.4f)", latitude, longitude)
        else -> stringResource(R.string.no_location_selected)
    }

    Text(
        text = headline,
        style = LiteverTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (hasError) LiteverTheme.colors.error else LiteverTheme.colors.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )

    Spacer(modifier = Modifier.height(2.dp))

    if (hasError) {
        Text(
            text = stringResource(R.string.reminder_location_required),
            style = LiteverTheme.typography.bodySmall,
            color = LiteverTheme.colors.error
        )
    } else {
        val transitionStr = when (transitionType) {
            GeofenceTransitionType.ENTER -> stringResource(R.string.transition_enter)
            GeofenceTransitionType.EXIT -> stringResource(R.string.transition_exit)
            GeofenceTransitionType.BOTH -> stringResource(R.string.transition_both)
        }

        val radiusStr = "${radiusMeters.toInt()}m"
        val windowStr = if (timeWindows.isNotEmpty()) {
            " • " + timeWindows.joinToString(", ") { window ->
                "${TimeFormatUtils.formatTime(window.startTime, is24HourFormat)}-${TimeFormatUtils.formatTime(window.endTime, is24HourFormat)}"
            }
        } else {
            ""
        }

        Text(
            text = "$transitionStr • $radiusStr$windowStr",
            style = LiteverTheme.typography.bodySmall,
            color = LiteverTheme.colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WifiTriggerSummary(
    ssid: String,
    triggerType: WifiTriggerType,
    timeWindows: List<ReminderTimeWindow>,
    is24HourFormat: Boolean,
    hasError: Boolean
) {
    val headline = if (ssid.isNotBlank()) ssid else stringResource(R.string.no_wifi_selected)

    Text(
        text = headline,
        style = LiteverTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = if (hasError) LiteverTheme.colors.error else LiteverTheme.colors.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )

    Spacer(modifier = Modifier.height(2.dp))

    if (hasError) {
        Text(
            text = stringResource(R.string.reminder_wifi_required),
            style = LiteverTheme.typography.bodySmall,
            color = LiteverTheme.colors.error
        )
    } else {
        val eventStr = when (triggerType) {
            WifiTriggerType.ON_CONNECT -> stringResource(R.string.wifi_on_connect)
            WifiTriggerType.ON_DISCONNECT -> stringResource(R.string.wifi_on_disconnect)
            WifiTriggerType.WHILE_CONNECTED -> stringResource(R.string.wifi_while_connected)
        }

        val windowStr = if (timeWindows.isNotEmpty()) {
            " • " + timeWindows.joinToString(", ") { window ->
                "${TimeFormatUtils.formatTime(window.startTime, is24HourFormat)}-${TimeFormatUtils.formatTime(window.endTime, is24HourFormat)}"
            }
        } else {
            ""
        }

        Text(
            text = "$eventStr$windowStr",
            style = LiteverTheme.typography.bodySmall,
            color = LiteverTheme.colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDayOfWeek(context: Context, day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> context.getString(R.string.day_mon)
        DayOfWeek.TUESDAY -> context.getString(R.string.day_tue)
        DayOfWeek.WEDNESDAY -> context.getString(R.string.day_wed)
        DayOfWeek.THURSDAY -> context.getString(R.string.day_thu)
        DayOfWeek.FRIDAY -> context.getString(R.string.day_fri)
        DayOfWeek.SATURDAY -> context.getString(R.string.day_sat)
        DayOfWeek.SUNDAY -> context.getString(R.string.day_sun)
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTriggerSectionTimePreview() {
    ReMindTheme {
        ReminderTriggerSection(
            triggerType = ReminderTriggerType.TIME,
            time = LocalTime.of(8, 30),
            date = LocalDate.now(),
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            repeatInterval = ReminderRepeatInterval.WEEKLY,
            monthlyDays = emptyList(),
            yearlyDates = emptyList(),
            locationName = "",
            latitude = 0.0,
            longitude = 0.0,
            radiusMeters = 100f,
            geofenceTransitionType = GeofenceTransitionType.ENTER,
            locationTimeWindows = emptyList(),
            wifiSsid = "",
            wifiTriggerType = WifiTriggerType.ON_CONNECT,
            wifiTimeWindows = emptyList(),
            is24HourFormat = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTriggerSectionLocationPreview() {
    ReMindTheme {
        ReminderTriggerSection(
            triggerType = ReminderTriggerType.LOCATION,
            time = LocalTime.of(8, 30),
            date = null,
            repeatDays = emptyList(),
            repeatInterval = ReminderRepeatInterval.ONCE,
            monthlyDays = emptyList(),
            yearlyDates = emptyList(),
            locationName = "Nhà riêng",
            latitude = 10.7626,
            longitude = 106.6601,
            radiusMeters = 150f,
            geofenceTransitionType = GeofenceTransitionType.ENTER,
            locationTimeWindows = listOf(
                ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(18, 0))
            ),
            wifiSsid = "",
            wifiTriggerType = WifiTriggerType.ON_CONNECT,
            wifiTimeWindows = emptyList(),
            is24HourFormat = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderTriggerSectionDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderTriggerSection(
            triggerType = ReminderTriggerType.WIFI,
            time = LocalTime.of(9, 0),
            date = null,
            repeatDays = emptyList(),
            repeatInterval = ReminderRepeatInterval.ONCE,
            monthlyDays = emptyList(),
            yearlyDates = emptyList(),
            locationName = "",
            latitude = 0.0,
            longitude = 0.0,
            radiusMeters = 100f,
            geofenceTransitionType = GeofenceTransitionType.ENTER,
            locationTimeWindows = emptyList(),
            wifiSsid = "Coffee_Free_WiFi",
            wifiTriggerType = WifiTriggerType.ON_CONNECT,
            wifiTimeWindows = emptyList(),
            is24HourFormat = true,
            onClick = {}
        )
    }
}
