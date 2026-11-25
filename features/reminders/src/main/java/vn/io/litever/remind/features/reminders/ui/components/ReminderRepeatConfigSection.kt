package vn.io.litever.remind.features.reminders.ui.components

import vn.io.litever.brand.tokens.LiteverTokens
import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier,
    exactDates: List<LocalDate> = if (date != null) listOf(date) else emptyList(),
    onAddExactDate: (LocalDate) -> Unit = { onDateChange(it) },
    onRemoveExactDate: (Int) -> Unit = {},
    onUpdateExactDate: (Int, LocalDate) -> Unit = { _, d -> onDateChange(d) }
) {
    val currentLocale = LocalLocale.current.platformLocale
    var showDatePicker by remember { mutableStateOf(false) }
    var editingExactDateIndex by remember { mutableStateOf<Int?>(null) }
    var editingExactDateInitial by remember { mutableStateOf<LocalDate?>(null) }
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
            ReminderRepeatInterval.DAILY,
            ReminderRepeatInterval.WEEKLY,
            ReminderRepeatInterval.MONTHLY,
            ReminderRepeatInterval.YEARLY
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = intervals,
                key = { it.name },
                contentType = { "repeat_chip" }
            ) { interval ->
                val labelRes = when (interval) {
                    ReminderRepeatInterval.NONE -> R.string.repeat_interval_once
                    ReminderRepeatInterval.DAILY -> R.string.repeat_interval_daily
                    ReminderRepeatInterval.WEEKLY -> R.string.repeat_interval_weekly
                    ReminderRepeatInterval.MONTHLY -> R.string.repeat_interval_monthly
                    ReminderRepeatInterval.YEARLY -> R.string.repeat_interval_yearly
                    else -> R.string.repeat_interval_once
                }
                val isSelected = repeatInterval == interval
                FilterChip(
                    selected = isSelected,
                    onClick = { onRepeatIntervalChange(interval) },
                    label = {
                        Text(
                            text = stringResource(labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }

        // Connected Detail Panel
        if (repeatInterval != ReminderRepeatInterval.DAILY && repeatInterval != ReminderRepeatInterval.CUSTOM) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .padding(bottom = MaterialTheme.spacing.medium),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                AnimatedContent(
                    targetState = repeatInterval,
                    label = "repeat_interval_content"
                ) { targetInterval ->
                    RepeatDetailContent(
                        repeatInterval = targetInterval,
                        date = date,
                        exactDates = exactDates,
                        onRemoveExactDate = onRemoveExactDate,
                        onShowAddExactDatePicker = {
                            editingExactDateIndex = null
                            editingExactDateInitial = null
                            showDatePicker = true
                        },
                        onShowEditExactDatePicker = { index, targetDate ->
                            editingExactDateIndex = index
                            editingExactDateInitial = targetDate
                            showDatePicker = true
                        },
                        repeatDays = repeatDays,
                        onRepeatDayToggle = onRepeatDayToggle,
                        monthlyDays = monthlyDays,
                        onMonthlyDayToggle = onMonthlyDayToggle,
                        yearlyDates = yearlyDates,
                        onRemoveYearlyDate = onRemoveYearlyDate,
                        onShowAddYearlyDateDialog = { showAddYearlyDateDialog = true },
                        onShowEditYearlyDateDialog = { index, entry ->
                            editingDateIndex = index
                            dateToEdit = entry
                            showEditYearlyDateDialog = true
                        }
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        }
    }

    // DatePicker Dialog for Date Mode (Add / Edit exact date)
    if (showDatePicker) {
        val seedDate = editingExactDateInitial ?: date ?: LocalDate.now()
        val initialEpochMillis = seedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
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

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
                editingExactDateIndex = null
                editingExactDateInitial = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            val activeIdx = editingExactDateIndex
                            if (activeIdx != null) {
                                onUpdateExactDate(activeIdx, selectedDate)
                            } else {
                                onAddExactDate(selectedDate)
                            }
                            onRepeatDaysChange(emptyList())
                        }
                        showDatePicker = false
                        editingExactDateIndex = null
                        editingExactDateInitial = null
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDatePicker = false
                        editingExactDateIndex = null
                        editingExactDateInitial = null
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        stringResource(R.string.set_reminder_date),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(
                            top = MaterialTheme.spacing.medium,
                            bottom = MaterialTheme.spacing.small,
                            start = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.medium
                        )
                    )
                }
            )
        }
    }

    // Add Yearly Date Dialog
    if (showAddYearlyDateDialog) {
        var selectedMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
        var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }

        AlertDialog(
            onDismissRequest = { showAddYearlyDateDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.add_date),
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    // Month dropdown
                    var monthExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { monthExpanded = true },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Month.of(selectedMonth)
                                            .getDisplayName(TextStyle.FULL, currentLocale),
                                        style = LiteverTokens.typography.bodyMediumEmphasized,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = ReMindIcons.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            color = if (m == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.day_format, selectedDay),
                                        style = LiteverTokens.typography.bodyMediumEmphasized,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = ReMindIcons.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            color = if (d == selectedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                Button(
                    onClick = {
                        onAddYearlyDate(selectedMonth, selectedDay)
                        showAddYearlyDateDialog = false
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddYearlyDateDialog = false }) {
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

        AlertDialog(
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
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    // Month dropdown
                    var monthExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { monthExpanded = true },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Month.of(selectedMonth)
                                            .getDisplayName(TextStyle.FULL, currentLocale),
                                        style = LiteverTokens.typography.bodyMediumEmphasized,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = ReMindIcons.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            color = if (m == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.day_format, selectedDay),
                                        style = LiteverTokens.typography.bodyMediumEmphasized,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = ReMindIcons.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            color = if (d == selectedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                Button(
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
                OutlinedButton(
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
    exactDates: List<LocalDate>,
    onRemoveExactDate: (Int) -> Unit,
    onShowAddExactDatePicker: () -> Unit,
    onShowEditExactDatePicker: (Int, LocalDate) -> Unit,
    repeatDays: List<DayOfWeek>,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    monthlyDays: List<Int>,
    onMonthlyDayToggle: (Int) -> Unit,
    yearlyDates: List<ReminderMonthDay>,
    onRemoveYearlyDate: (Int) -> Unit,
    onShowAddYearlyDateDialog: () -> Unit,
    onShowEditYearlyDateDialog: (Int, ReminderMonthDay) -> Unit
) {
    val currentLocale = LocalLocale.current.platformLocale

    when (repeatInterval) {
        ReminderRepeatInterval.NONE -> {
            val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
            val datesList = exactDates.ifEmpty { listOfNotNull(date) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.smallMedium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(R.string.selected_dates),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    datesList.forEachIndexed { index, targetDate ->
                        val label = targetDate.format(dateFormatter)

                        InputChip(
                            selected = true,
                            onClick = { onShowEditExactDatePicker(index, targetDate) },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = if (datesList.size > 1) {
                                {
                                    Icon(
                                        imageVector = ReMindIcons.Close,
                                        contentDescription = "Remove Date",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { onRemoveExactDate(index) }
                                    )
                                }
                            } else null,
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }

                    InputChip(
                        selected = false,
                        onClick = { onShowAddExactDatePicker() },
                        label = {
                            Text(
                                stringResource(R.string.add_date),
                                style = LiteverTokens.typography.labelMediumEmphasized
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        ReminderRepeatInterval.WEEKLY -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.smallMedium),
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
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onRepeatDayToggle(day) },
                        color = if (isSelected) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        border = if (isSelected) null else BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(dayLabelRes),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        ReminderRepeatInterval.MONTHLY -> {
            Column(modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.smallMedium)) {
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
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
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
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable { onMonthlyDayToggle(dayNumber) },
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small,
                                    border = if (isSelected) null else BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
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
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.smallMedium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = stringResource(R.string.selected_dates),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    yearlyDates.forEachIndexed { index, dateEntry ->
                        val monthName =
                            Month.of(dateEntry.month).getDisplayName(TextStyle.SHORT, currentLocale)
                        val label = "${dateEntry.day} $monthName"

                        InputChip(
                            selected = true,
                            onClick = { onShowEditYearlyDateDialog(index, dateEntry) },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
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
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                                style = LiteverTokens.typography.labelMediumEmphasized
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
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
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
