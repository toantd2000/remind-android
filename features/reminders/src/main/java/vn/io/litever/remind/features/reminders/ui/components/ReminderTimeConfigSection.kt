package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverDatePickerDialog
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverListItem
import vn.io.litever.designsystem.components.LiteverModalBottomSheet
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverRadioButton
import vn.io.litever.designsystem.components.LiteverTimePickerDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimeConfigSection(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    repeatDays: List<DayOfWeek>,
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    repeatInterval: ReminderRepeatInterval,
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit,
    monthlyDay: Int,
    onMonthlyDayChange: (Int) -> Unit,
    yearlyMonth: Int,
    yearlyDay: Int,
    onYearlyMonthAndDayChange: (Int, Int) -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRepeatSheet by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LiteverOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_time_trigger),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Time Setting Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { showTimePicker = true },
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ReMindSettingIcon(
                            imageVector = Icons.Rounded.AccessTime,
                            selected = false
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.select_time),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )
                            Text(
                                text = TimeFormatUtils.formatTime(time, is24HourFormat),
                                style = LiteverTheme.typography.bodyMedium,
                                color = LiteverTheme.colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Repeat Interval Setting Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { showRepeatSheet = true },
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ReMindSettingIcon(
                            imageVector = Icons.Rounded.Repeat,
                            selected = repeatInterval != ReminderRepeatInterval.ONCE
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.repeat_title),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )

                            val repeatSummary = when (repeatInterval) {
                                ReminderRepeatInterval.ONCE -> stringResource(R.string.repeat_interval_once)
                                ReminderRepeatInterval.DAILY -> stringResource(R.string.repeat_interval_daily)
                                ReminderRepeatInterval.WEEKLY -> {
                                    if (repeatDays.isEmpty()) {
                                        stringResource(R.string.repeat_interval_weekly)
                                    } else {
                                        val dayNames = repeatDays.joinToString(", ") { context.getString(getDayResId(it)) }
                                        "${stringResource(R.string.repeat_interval_weekly)} ($dayNames)"
                                    }
                                }
                                ReminderRepeatInterval.MONTHLY -> stringResource(R.string.repeat_monthly_day_summary, monthlyDay)
                                ReminderRepeatInterval.YEARLY -> {
                                    val monthName = Month.of(yearlyMonth).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                    stringResource(R.string.repeat_yearly_summary, monthName, yearlyDay)
                                }
                            }

                            Text(
                                text = repeatSummary,
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Sub-controls based on Repeat Interval Mode
            when (repeatInterval) {
                ReminderRepeatInterval.ONCE -> {
                    // One-time: Show Date Selector Row
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LiteverTheme.shapes.medium)
                            .clickable { showDatePicker = true },
                        color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                        shape = LiteverTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                ReMindSettingIcon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    selected = date != null
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.select_date),
                                        style = LiteverTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LiteverTheme.colors.onSurface
                                    )
                                    Text(
                                        text = if (date != null) {
                                            date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy"))
                                        } else {
                                            stringResource(R.string.select_date)
                                        },
                                        style = LiteverTheme.typography.bodySmall,
                                        color = if (date != null) LiteverTheme.colors.onSurface else LiteverTheme.colors.onSurfaceVariant
                                    )
                                }
                            }

                            if (date != null) {
                                LiteverIconButton(
                                    onClick = { onDateChange(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Clear Date",
                                        tint = LiteverTheme.colors.onSurfaceVariant
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                ReminderRepeatInterval.DAILY -> {
                    // Daily: Do not show any further day/date picker.
                }

                ReminderRepeatInterval.WEEKLY -> {
                    // Weekly: Display a single row of day-of-week buttons evenly distributed in width.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            val isSelected = repeatDays.contains(day)
                            val dayLabelRes = when (day) {
                                DayOfWeek.MONDAY -> R.string.day_mon
                                DayOfWeek.TUESDAY -> R.string.day_tue
                                DayOfWeek.WEDNESDAY -> R.string.day_wed
                                DayOfWeek.THURSDAY -> R.string.day_thu
                                DayOfWeek.FRIDAY -> R.string.day_fri
                                DayOfWeek.SATURDAY -> R.string.day_sat
                                DayOfWeek.SUNDAY -> R.string.day_sun
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(LiteverTheme.shapes.medium)
                                    .clickable { onRepeatDayToggle(day) },
                                color = if (isSelected) LiteverTheme.colors.primaryContainer
                                else LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                                shape = LiteverTheme.shapes.medium,
                                border = if (isSelected) null else BorderStroke(
                                    1.dp,
                                    LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = stringResource(dayLabelRes),
                                        style = LiteverTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) LiteverTheme.colors.onPrimaryContainer
                                        else LiteverTheme.colors.onSurfaceVariant,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }

                ReminderRepeatInterval.MONTHLY -> {
                    // Monthly: Display a grid to select a specific day (1-31).
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.day_of_month_format, monthlyDay),
                            style = LiteverTheme.typography.labelMedium,
                            color = LiteverTheme.colors.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val totalDays = 31
                        val rows = (totalDays + 6) / 7
                        for (r in 0 until rows) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (c in 0 until 7) {
                                    val dayNumber = r * 7 + c + 1
                                    if (dayNumber <= totalDays) {
                                        val isSelected = dayNumber == monthlyDay
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .clip(LiteverTheme.shapes.small)
                                                .clickable { onMonthlyDayChange(dayNumber) },
                                            color = if (isSelected) LiteverTheme.colors.primaryContainer
                                            else LiteverTheme.colors.surfaceVariant.copy(alpha = 0.35f),
                                            shape = LiteverTheme.shapes.small,
                                            border = if (isSelected) null else BorderStroke(
                                                1.dp,
                                                LiteverTheme.colors.outlineVariant.copy(alpha = 0.15f)
                                            )
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    style = LiteverTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = if (isSelected) LiteverTheme.colors.onPrimaryContainer
                                                    else LiteverTheme.colors.onSurfaceVariant,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                ReminderRepeatInterval.YEARLY -> {
                    // Yearly: Display two dropdown menus: Month and Day (1-31).
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Month Dropdown
                        var monthExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            val currentMonth = Month.of(yearlyMonth)
                            val monthDisplayName = currentMonth.getDisplayName(TextStyle.FULL, Locale.getDefault())

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(LiteverTheme.shapes.medium)
                                    .clickable { monthExpanded = true },
                                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                                shape = LiteverTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.select_month_label),
                                            style = LiteverTheme.typography.labelSmall,
                                            color = LiteverTheme.colors.onSurfaceVariant
                                        )
                                        Text(
                                            text = monthDisplayName,
                                            style = LiteverTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LiteverTheme.colors.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDropDown,
                                        contentDescription = null,
                                        tint = LiteverTheme.colors.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = monthExpanded,
                                onDismissRequest = { monthExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                (1..12).forEach { m ->
                                    val mName = Month.of(m).getDisplayName(TextStyle.FULL, Locale.getDefault())
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = mName,
                                                fontWeight = if (m == yearlyMonth) FontWeight.Bold else FontWeight.Normal,
                                                color = if (m == yearlyMonth) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                                            )
                                        },
                                        onClick = {
                                            val maxDays = Month.of(m).maxLength()
                                            val clampedDay = yearlyDay.coerceAtMost(maxDays)
                                            onYearlyMonthAndDayChange(m, clampedDay)
                                            monthExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Day Dropdown
                        var dayExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            val maxDaysInMonth = Month.of(yearlyMonth).maxLength()
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(LiteverTheme.shapes.medium)
                                    .clickable { dayExpanded = true },
                                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                                shape = LiteverTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = stringResource(R.string.select_day_label),
                                            style = LiteverTheme.typography.labelSmall,
                                            color = LiteverTheme.colors.onSurfaceVariant
                                        )
                                        Text(
                                            text = stringResource(R.string.day_format, yearlyDay),
                                            style = LiteverTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LiteverTheme.colors.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDropDown,
                                        contentDescription = null,
                                        tint = LiteverTheme.colors.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = dayExpanded,
                                onDismissRequest = { dayExpanded = false },
                                modifier = Modifier.heightIn(max = 280.dp)
                            ) {
                                (1..maxDaysInMonth).forEach { d ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(R.string.day_format, d),
                                                fontWeight = if (d == yearlyDay) FontWeight.Bold else FontWeight.Normal,
                                                color = if (d == yearlyDay) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                                            )
                                        },
                                        onClick = {
                                            onYearlyMonthAndDayChange(yearlyMonth, d)
                                            dayExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Repeat Interval Modal Bottom Sheet
    if (showRepeatSheet) {
        LiteverModalBottomSheet(
            onDismissRequest = { showRepeatSheet = false },
            containerColor = LiteverTheme.colors.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.select_repeat_interval),
                    style = LiteverTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp),
                    color = LiteverTheme.colors.onSurface
                )

                ReminderRepeatInterval.entries.forEach { interval ->
                    val labelRes = when (interval) {
                        ReminderRepeatInterval.ONCE -> R.string.repeat_interval_once
                        ReminderRepeatInterval.DAILY -> R.string.repeat_interval_daily
                        ReminderRepeatInterval.WEEKLY -> R.string.repeat_interval_weekly
                        ReminderRepeatInterval.MONTHLY -> R.string.repeat_interval_monthly
                        ReminderRepeatInterval.YEARLY -> R.string.repeat_interval_yearly
                    }
                    val isSelected = interval == repeatInterval

                    LiteverListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(labelRes),
                                style = LiteverTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                            )
                        },
                        leadingContent = {
                            LiteverRadioButton(
                                selected = isSelected,
                                onClick = {
                                    onRepeatIntervalChange(interval)
                                    showRepeatSheet = false
                                }
                            )
                        },
                        modifier = Modifier.clickable {
                            onRepeatIntervalChange(interval)
                            showRepeatSheet = false
                        }
                    )
                }
            }
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = is24HourFormat
        )
        LiteverTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                LiteverButton(onClick = {
                    onTimeChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val initialEpochMillis = date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialEpochMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val d = Instant.ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    return !d.isBefore(LocalDate.now())
                }
            }
        )

        LiteverDatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                LiteverButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            onDateChange(selectedDate)
                            onRepeatDaysChange(emptyList()) // Specific date clears repeat days
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun getDayLabel(day: DayOfWeek): String {
    return stringResource(getDayResId(day))
}

private fun getDayResId(day: DayOfWeek): Int {
    return when (day) {
        DayOfWeek.MONDAY -> R.string.day_mon
        DayOfWeek.TUESDAY -> R.string.day_tue
        DayOfWeek.WEDNESDAY -> R.string.day_wed
        DayOfWeek.THURSDAY -> R.string.day_thu
        DayOfWeek.FRIDAY -> R.string.day_fri
        DayOfWeek.SATURDAY -> R.string.day_sat
        DayOfWeek.SUNDAY -> R.string.day_sun
    }
}


