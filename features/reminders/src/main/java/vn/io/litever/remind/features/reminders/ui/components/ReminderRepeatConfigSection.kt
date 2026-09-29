package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
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
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.designsystem.components.LiteverSegmentedButton
import vn.io.litever.designsystem.components.LiteverSingleChoiceSegmentedButtonRow
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderRepeatConfigSection(
    repeatInterval: ReminderRepeatInterval,
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    repeatDays: List<DayOfWeek>,
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    monthlyDays: List<Int>,
    onMonthlyDayToggle: (Int) -> Unit,
    yearlyDates: List<ReminderMonthDay>,
    onAddYearlyDate: (Int, Int) -> Unit,
    onRemoveYearlyDate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddYearlyDateDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.repeat_title),
            style = LiteverTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = LiteverTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LiteverSingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            val intervals = listOf(
                ReminderRepeatInterval.NONE,
                ReminderRepeatInterval.WEEKLY,
                ReminderRepeatInterval.MONTHLY,
                ReminderRepeatInterval.YEARLY
            )
            intervals.forEach { interval ->
                val labelRes = when (interval) {
                    ReminderRepeatInterval.NONE -> R.string.repeat_interval_once
                    ReminderRepeatInterval.WEEKLY -> R.string.repeat_interval_weekly
                    ReminderRepeatInterval.MONTHLY -> R.string.repeat_interval_monthly
                    ReminderRepeatInterval.YEARLY -> R.string.repeat_interval_yearly
                    else -> R.string.repeat_interval_once
                }
                LiteverSegmentedButton(
                    onClick = { onRepeatIntervalChange(interval) },
                    selected = repeatInterval == interval,
                    label = {
                        Text(
                            text = stringResource(labelRes),
                            style = LiteverTheme.typography.labelSmall
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (repeatInterval) {
            ReminderRepeatInterval.NONE -> {
                // One-time: Date selector row
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
                                imageVector = ReMindIcons.CalendarMonth,
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
                                        date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy"))
                                    } else {
                                        stringResource(R.string.select_date)
                                    },
                                    style = LiteverTheme.typography.bodySmall,
                                    color = if (date != null) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant
                                )
                            }
                        }

                        if (date != null) {
                            LiteverIconButton(
                                onClick = { onDateChange(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = ReMindIcons.Clear,
                                    contentDescription = "Clear Date",
                                    tint = LiteverTheme.colors.onSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                imageVector = ReMindIcons.KeyboardArrowRight,
                                contentDescription = null,
                                tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            ReminderRepeatInterval.WEEKLY -> {
                // Weekly: 7 day pills (Mon..Sun)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DayOfWeek.values().forEach { day ->
                        val isSelected = repeatDays.contains(day)
                        val dayLabelRes = when (day) {
                            DayOfWeek.MONDAY -> R.string.day_mon
                            DayOfWeek.TUESDAY -> R.string.day_tue
                            DayOfWeek.WEDNESDAY -> R.string.day_wed
                            DayOfWeek.THURSDAY -> R.string.day_thu
                            DayOfWeek.FRIDAY -> R.string.day_fri
                            DayOfWeek.SATURDAY -> R.string.day_sat
                            DayOfWeek.SUNDAY -> R.string.day_sun
                            else -> R.string.day_mon
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
                // Monthly: Multi-day selection grid (1..31)
                Column(modifier = Modifier.fillMaxWidth()) {
                    val selectedSummary = if (monthlyDays.isEmpty()) {
                        stringResource(R.string.select_day_of_month)
                    } else {
                        stringResource(R.string.monthly_days_selected_format, monthlyDays.joinToString(", "))
                    }

                    Text(
                        text = selectedSummary,
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
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until 7) {
                                val dayNumber = r * 7 + c + 1
                                if (dayNumber <= totalDays) {
                                    val isSelected = monthlyDays.contains(dayNumber)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(LiteverTheme.shapes.small)
                                            .clickable { onMonthlyDayToggle(dayNumber) },
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

            ReminderRepeatInterval.DAILY, ReminderRepeatInterval.CUSTOM -> {}
            ReminderRepeatInterval.YEARLY -> {
                // Yearly: Multi-date list with chip items and "+ Add Date" button
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.selected_dates),
                            style = LiteverTheme.typography.labelMedium,
                            color = LiteverTheme.colors.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )

                        LiteverOutlinedButton(
                            onClick = { showAddYearlyDateDialog = true },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = ReMindIcons.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.add_date),
                                style = LiteverTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        yearlyDates.forEachIndexed { index, dateEntry ->
                            val monthName = Month.of(dateEntry.month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            val label = "${dateEntry.day} $monthName"

                            InputChip(
                                selected = true,
                                onClick = {},
                                label = { Text(label, style = LiteverTheme.typography.labelMedium) },
                                trailingIcon = if (yearlyDates.size > 1) {
                                    {
                                        Icon(
                                            imageVector = ReMindIcons.Close,
                                            contentDescription = "Remove Date",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { onRemoveYearlyDate(index) }
                                        )
                                    }
                                } else null,
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = LiteverTheme.colors.primaryContainer,
                                    selectedLabelColor = LiteverTheme.colors.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // DatePicker Dialog for One-time
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
                            onRepeatDaysChange(emptyList())
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

    // Add Yearly Date Dialog
    if (showAddYearlyDateDialog) {
        var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue) }
        var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfMonth) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddYearlyDateDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.add_date),
                    style = LiteverTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month dropdown
                    var monthExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
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
                                        text = Month.of(selectedMonth).getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                        style = LiteverTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LiteverTheme.colors.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = ReMindIcons.ArrowDropDown,
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
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = Month.of(m).getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                            fontWeight = if (m == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                            color = if (m == selectedMonth) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedMonth = m
                                        val maxD = Month.of(m).maxLength()
                                        if (selectedDay > maxD) selectedDay = maxD
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Day dropdown
                    var dayExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        val maxDaysInMonth = Month.of(selectedMonth).maxLength()
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
                                        text = stringResource(R.string.day_format, selectedDay),
                                        style = LiteverTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = LiteverTheme.colors.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = ReMindIcons.ArrowDropDown,
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
                                            fontWeight = if (d == selectedDay) FontWeight.Bold else FontWeight.Normal,
                                            color = if (d == selectedDay) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedDay = d
                                        dayExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                LiteverButton(
                    onClick = {
                        onAddYearlyDate(selectedMonth, selectedDay)
                        showAddYearlyDateDialog = false
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                LiteverOutlinedButton(onClick = { showAddYearlyDateDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}
