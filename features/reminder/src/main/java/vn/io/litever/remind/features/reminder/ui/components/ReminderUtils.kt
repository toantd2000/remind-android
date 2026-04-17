package vn.io.litever.remind.features.reminder.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.features.reminder.R
import java.time.LocalTime as JavaLocalTime

@Composable
fun getRepeatSummaryText(
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
    
    if (repeatDays.size == 7) {
        return stringResource(R.string.every_day)
    }

    val context = LocalContext.current
    return repeatDays.sortedBy { it.ordinal }.joinToString(", ") { day ->
        when (day) {
            DayOfWeek.MONDAY -> context.getString(R.string.day_mon)
            DayOfWeek.TUESDAY -> context.getString(R.string.day_tue)
            DayOfWeek.WEDNESDAY -> context.getString(R.string.day_wed)
            DayOfWeek.THURSDAY -> context.getString(R.string.day_thu)
            DayOfWeek.FRIDAY -> context.getString(R.string.day_fri)
            DayOfWeek.SATURDAY -> context.getString(R.string.day_sat)
            DayOfWeek.SUNDAY -> context.getString(R.string.day_sun)
        }
    }
}
