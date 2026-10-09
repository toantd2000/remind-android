package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.intl.Locale as ComposeLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverDatePickerDialog
import vn.io.litever.designsystem.components.LiteverFilterChip
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.designsystem.components.dialog.LvAlertDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

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
    val currentLocale = ComposeLocale.current.platformLocale
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddYearlyDateDialog by remember { mutableStateOf(false) }
    var showEditYearlyDateDialog by remember { mutableStateOf(false) }
    var editingDateIndex by remember { mutableIntStateOf(-1) }
    var dateToEdit by remember { mutableStateOf<ReminderMonthDay?>(null) }

    ReMindSettingsGroup(
        title = stringResource(R.string.repeat_title),
        modifier = modifier
    ) {
        val intervals = listOf(
            ReminderRepeatInterval.NONE,
            ReminderRepeatInterval.WEEKLY,
            ReminderRepeatInterval.MONTHLY,
            ReminderRepeatInterval.YEARLY
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = LiteverTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(intervals) { interval ->
                val labelRes = when (interval) {
                    ReminderRepeatInterval.NONE -> R.string.repeat_interval_once
                    ReminderRepeatInterval.WEEKLY -> R.string.repeat_interval_weekly
                    ReminderRepeatInterval.MONTHLY -> R.string.repeat_interval_monthly
                    ReminderRepeatInterval.YEARLY -> R.string.repeat_interval_yearly
                    else -> R.string.repeat_interval_once
                }
                val isSelected = repeatInterval == interval
                LiteverFilterChip(
                    selected = isSelected,
                    onClick = { onRepeatIntervalChange(interval) },
                    label = {
                        Text(
                            text = stringResource(labelRes),
                            style = LiteverTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = LiteverTheme.colors.surfaceVariant,
                        labelColor = LiteverTheme.colors.onSurfaceVariant,
                        selectedContainerColor = LiteverTheme.colors.primary,
                        selectedLabelColor = LiteverTheme.colors.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = LiteverTheme.colors.outlineVariant,
                        selectedBorderColor = LiteverTheme.colors.outline
                    ),
                    shape = LiteverTheme.shapes.medium
                )
            }
        }

        // Connected Detail Panel
        Surface(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = LiteverTheme.spacing.medium)
                .padding(bottom = LiteverTheme.spacing.medium)
            ,
            shape = LiteverTheme.shapes.large,
            color = LiteverTheme.colors.surfaceContainer,
            border = BorderStroke(1.dp, LiteverTheme.colors.outlineVariant)
        ) {
            AnimatedContent(
                targetState = repeatInterval,
                label = "repeat_interval_content"
            ) { targetInterval ->
                RepeatDetailContent(
                    repeatInterval = targetInterval,
                    date = date,
                    onDateChange = onDateChange,
                    repeatDays = repeatDays,
                    onRepeatDayToggle = onRepeatDayToggle,
                    monthlyDays = monthlyDays,
                    onMonthlyDayToggle = onMonthlyDayToggle,
                    yearlyDates = yearlyDates,
                    onRemoveYearlyDate = onRemoveYearlyDate,
                    onShowDatePicker = { showDatePicker = true },
                    onShowAddYearlyDateDialog = { showAddYearlyDateDialog = true },
                    onShowEditYearlyDateDialog = { index, entry ->
                        editingDateIndex = index
                        dateToEdit = entry
                        showEditYearlyDateDialog = true
                    }
                )
            }

        }
    }

    // DatePicker Dialog for One-time
    if (showDatePicker) {
        val initialEpochMillis =
            date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
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
        var selectedMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
        var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }

        LvAlertDialog(
            onDismissRequest = { showAddYearlyDateDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.add_date),
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    // Month dropdown
                    var monthExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { monthExpanded = true },
                            color = LiteverTheme.colors.surfaceVariant,
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
                                        text = Month.of(selectedMonth)
                                            .getDisplayName(TextStyle.FULL, currentLocale),
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
                                            text = Month.of(m)
                                                .getDisplayName(TextStyle.FULL, currentLocale),
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
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { dayExpanded = true },
                            color = LiteverTheme.colors.surfaceVariant,
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

    // Edit Yearly Date Dialog
    val currentToEdit = dateToEdit
    if (showEditYearlyDateDialog && currentToEdit != null) {
        var selectedMonth by remember(currentToEdit) { mutableIntStateOf(currentToEdit.month) }
        var selectedDay by remember(currentToEdit) { mutableIntStateOf(currentToEdit.day) }

        LvAlertDialog(
            onDismissRequest = {
                showEditYearlyDateDialog = false
                dateToEdit = null
                editingDateIndex = -1
            },
            title = {
                Text(
                    text = stringResource(R.string.edit_date),
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    // Month dropdown
                    var monthExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { monthExpanded = true },
                            color = LiteverTheme.colors.surfaceVariant,
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
                                        text = Month.of(selectedMonth)
                                            .getDisplayName(TextStyle.FULL, currentLocale),
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
                                            text = Month.of(m)
                                                .getDisplayName(TextStyle.FULL, currentLocale),
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
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { dayExpanded = true },
                            color = LiteverTheme.colors.surfaceVariant,
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
                        if (editingDateIndex in yearlyDates.indices) {
                            onRemoveYearlyDate(editingDateIndex)
                        }
                        onAddYearlyDate(selectedMonth, selectedDay)
                        showEditYearlyDateDialog = false
                        dateToEdit = null
                        editingDateIndex = -1
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                LiteverOutlinedButton(
                    onClick = {
                        showEditYearlyDateDialog = false
                        dateToEdit = null
                        editingDateIndex = -1
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun RepeatDetailContent(
    repeatInterval: ReminderRepeatInterval,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    repeatDays: List<DayOfWeek>,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    monthlyDays: List<Int>,
    onMonthlyDayToggle: (Int) -> Unit,
    yearlyDates: List<ReminderMonthDay>,
    onRemoveYearlyDate: (Int) -> Unit,
    onShowDatePicker: () -> Unit,
    onShowAddYearlyDateDialog: () -> Unit,
    onShowEditYearlyDateDialog: (Int, ReminderMonthDay) -> Unit
) {
    val currentLocale = ComposeLocale.current.platformLocale

    when (repeatInterval) {
        ReminderRepeatInterval.NONE -> {
            Surface(
                onClick = onShowDatePicker,
                color = LiteverTheme.colors.surfaceContainer,
                shape = LiteverTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = LiteverTheme.spacing.smallMedium
                        )
                        .padding(start = LiteverTheme.spacing.smallMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = LiteverTheme.shapes.medium,
                        color = if(date != null) LiteverTheme.colors.secondaryContainer else LiteverTheme.colors.surfaceVariant,
                        contentColor = if(date != null) LiteverTheme.colors.onSecondaryContainer else LiteverTheme.colors.onSurfaceVariant
                    ) {
                        Icon(
                            imageVector = ReMindIcons.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.padding(LiteverTheme.spacing.small),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (date != null) {
                                date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy"))
                            } else {
                                stringResource(R.string.select_date)
                            },
                            style = LiteverTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (date != null) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface
                        )
                        Text(
                            text = stringResource(R.string.tap_to_select_date),
                            style = LiteverTheme.typography.bodySmall,
                            color = LiteverTheme.colors.onSurfaceVariant
                        )
                    }

                    if (date != null) {
                        LiteverIconButton(
                            onClick = { onDateChange(null) },
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
                            tint = LiteverTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(end = LiteverTheme.spacing.small)
                        )
                    }
                }
            }
        }

        ReminderRepeatInterval.WEEKLY -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(LiteverTheme.spacing.smallMedium),
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
                        color = if (isSelected) LiteverTheme.colors.secondary
                        else LiteverTheme.colors.surfaceVariant,
                        shape = LiteverTheme.shapes.medium,
                        border = if (isSelected) null else BorderStroke(
                            1.dp,
                            LiteverTheme.colors.outlineVariant
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(dayLabelRes),
                                style = LiteverTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (isSelected) LiteverTheme.colors.onSecondary
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
            Column(modifier = Modifier.fillMaxWidth().padding(LiteverTheme.spacing.smallMedium)) {
                val selectedSummary = if (monthlyDays.isEmpty()) {
                    stringResource(R.string.select_day_of_month)
                } else {
                    stringResource(
                        R.string.monthly_days_selected_format,
                        monthlyDays.joinToString(", ")
                    )
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
                                    color = if (isSelected) LiteverTheme.colors.secondary
                                    else LiteverTheme.colors.surfaceVariant,
                                    shape = LiteverTheme.shapes.small,
                                    border = if (isSelected) null else BorderStroke(
                                        1.dp,
                                        LiteverTheme.colors.outlineVariant
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = LiteverTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                            ),
                                            color = if (isSelected) LiteverTheme.colors.onSecondary
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
            Column(
                modifier = Modifier.fillMaxWidth().padding(LiteverTheme.spacing.smallMedium),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(R.string.selected_dates),
                    style = LiteverTheme.typography.labelMedium,
                    color = LiteverTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall),
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall)
                ) {
                    yearlyDates.forEachIndexed { index, dateEntry ->
                        val monthName =
                            Month.of(dateEntry.month).getDisplayName(TextStyle.SHORT, currentLocale)
                        val label = "${dateEntry.day} $monthName"

                        InputChip(
                            selected = true,
                            onClick = { onShowEditYearlyDateDialog(index, dateEntry) },
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
                                selectedContainerColor = LiteverTheme.colors.secondary,
                                selectedLabelColor = LiteverTheme.colors.onSecondary,
                                selectedTrailingIconColor = LiteverTheme.colors.onSecondary,
                                containerColor = LiteverTheme.colors.surfaceVariant,
                                labelColor = LiteverTheme.colors.onSurfaceVariant,
                                leadingIconColor = LiteverTheme.colors.onSurfaceVariant
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }

                    InputChip(
                        selected = false,
                        onClick = { onShowAddYearlyDateDialog() },
                        label = {
                            Text(
                                stringResource(R.string.add_date),
                                style = LiteverTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = ReMindIcons.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = LiteverTheme.colors.surfaceVariant,
                            labelColor = LiteverTheme.colors.onSurfaceVariant
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderRepeatConfigSectionPreview() {
    ReMindTheme {
        ReminderRepeatConfigSection(
            repeatInterval = ReminderRepeatInterval.YEARLY,
            onRepeatIntervalChange = {},
            date = LocalDate.now(),
            onDateChange = {},
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            monthlyDays = emptyList(),
            onMonthlyDayToggle = {},
            yearlyDates = emptyList(),
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderRepeatConfigSectionDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderRepeatConfigSection(
            repeatInterval = ReminderRepeatInterval.NONE,
            onRepeatIntervalChange = {},
            date = LocalDate.now(),
            onDateChange = {},
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            monthlyDays = emptyList(),
            onMonthlyDayToggle = {},
            yearlyDates = emptyList(),
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {}
        )
    }
}
