package vn.io.litever.remind.features.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.features.reminders.ui.components.ReminderCardFormatter
import vn.io.litever.remind.features.reminders.ui.components.ReminderStringProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderCardFormatterTest {

    private lateinit var stringProvider: ReminderStringProvider

    @Before
    fun setup() {
        stringProvider = ReminderStringProvider { id, args ->
            when (id) {
                R.string.daily -> "Hàng ngày"
                R.string.once_only -> "Một lần"
                R.string.weekdays -> "T2 - T6"
                R.string.weekends -> "T7, CN"
                R.string.day_mon -> "T2"
                R.string.day_tue -> "T3"
                R.string.day_wed -> "T4"
                R.string.day_thu -> "T5"
                R.string.day_fri -> "T6"
                R.string.day_sat -> "T7"
                R.string.day_sun -> "CN"
                R.string.day_format -> "Ngày ${args.getOrNull(0)}"
                R.string.interval_hours_format -> "Mỗi ${args.getOrNull(0)} giờ"
                R.string.interval_hours_minutes_format -> "Mỗi ${args.getOrNull(0)}h${args.getOrNull(1)}p"
                R.string.interval_minutes_format -> "Mỗi ${args.getOrNull(0)} phút"
                R.string.time_window_all_day_badge -> "Cả ngày"
                R.string.reminder_status_disabled -> "Đã tắt"
                R.string.reminder_status_completed -> "Đã hoàn thành các lượt nhắc"
                R.string.next_trigger_today -> "Lần tới: Hôm nay ${args.getOrNull(0)} (${args.getOrNull(1)})"
                R.string.next_trigger_tomorrow -> "Lần tới: Ngày mai ${args.getOrNull(0)} (${args.getOrNull(1)})"
                R.string.next_trigger_upcoming_day -> "Lần tới: ${args.getOrNull(0)}, ${args.getOrNull(1)} lúc ${args.getOrNull(2)} (${args.getOrNull(3)})"
                R.string.next_trigger_far -> "Lần tới: ${args.getOrNull(0)} lúc ${args.getOrNull(1)}"
                R.string.next_trigger_snoozed -> "Báo lại: ${args.getOrNull(0)} (${args.getOrNull(1)})"
                R.string.remaining_minutes -> "sau ${args.getOrNull(0)} phút"
                R.string.remaining_hours -> "sau ${args.getOrNull(0)} giờ"
                R.string.remaining_hours_minutes -> "sau ${args.getOrNull(0)} giờ ${args.getOrNull(1)} phút"
                R.string.remaining_days -> "sau ${args.getOrNull(0)} ngày"
                R.string.remaining_soon -> "sắp đến"
                R.string.snooze_remaining_minutes -> "còn ${args.getOrNull(0)} phút"
                R.string.snooze_remaining_hours -> "còn ${args.getOrNull(0)} giờ"
                R.string.snooze_remaining_soon -> "sắp reo"
                else -> ""
            }
        }
    }

    @Test
    fun formatRecurrenceSchedule_daily_returnsDaily() {
        val trigger = TimeTrigger(repeatMode = RepeatMode.DAILY)
        val result = ReminderCardFormatter.formatRecurrenceSchedule(trigger, stringProvider)
        assertEquals("Hàng ngày", result)
    }

    @Test
    fun formatRecurrenceSchedule_weekdays_returnsWeekdays() {
        val trigger = TimeTrigger(repeatMode = RepeatMode.WEEKLY, repeatValues = listOf(1, 2, 3, 4, 5))
        val result = ReminderCardFormatter.formatRecurrenceSchedule(trigger, stringProvider)
        assertEquals("T2 - T6", result)
    }

    @Test
    fun formatRecurrenceSchedule_customWeekly_returnsFormattedDays() {
        val trigger = TimeTrigger(repeatMode = RepeatMode.WEEKLY, repeatValues = listOf(1, 3, 5))
        val result = ReminderCardFormatter.formatRecurrenceSchedule(trigger, stringProvider)
        assertEquals("T2, T4, T6", result)
    }

    @Test
    fun formatRecurrenceSchedule_monthly_returnsFormattedDays() {
        val trigger = TimeTrigger(repeatMode = RepeatMode.MONTHLY, monthlyDays = listOf(1, 15, 30))
        val result = ReminderCardFormatter.formatRecurrenceSchedule(trigger, stringProvider)
        assertEquals("Ngày 1, Ngày 15, Ngày 30", result)
    }

    @Test
    fun formatRecurrenceSchedule_yearly_returnsFormattedDates() {
        val trigger = TimeTrigger(
            repeatMode = RepeatMode.YEARLY,
            yearlyDates = listOf(ReminderMonthDay(month = 3, day = 8), ReminderMonthDay(month = 10, day = 20))
        )
        val result = ReminderCardFormatter.formatRecurrenceSchedule(trigger, stringProvider)
        assertEquals("08/03, 20/10", result)
    }

    @Test
    fun formatIntervalFrequency_exactHours_returnsHoursFormat() {
        val result = ReminderCardFormatter.formatIntervalFrequency(120, stringProvider)
        assertEquals("Mỗi 2 giờ", result)
    }

    @Test
    fun formatIntervalFrequency_minutes_returnsMinutesFormat() {
        val result = ReminderCardFormatter.formatIntervalFrequency(45, stringProvider)
        assertEquals("Mỗi 45 phút", result)
    }

    @Test
    fun formatIntervalFrequency_hoursAndMinutes_returnsHoursMinutesFormat() {
        val result = ReminderCardFormatter.formatIntervalFrequency(90, stringProvider)
        assertEquals("Mỗi 1h30p", result)
    }

    @Test
    fun formatTimeWindows_singleWindow_returnsFormattedTimeWindow() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 30))
        val result = ReminderCardFormatter.formatTimeWindows(listOf(window), is24HourFormat = true, provider = stringProvider)
        assertEquals("08:00 - 17:30", result)
    }

    @Test
    fun formatTimeWindows_allDay_returnsAllDayBadge() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(0, 0), endTime = LocalTime.of(23, 59))
        val result = ReminderCardFormatter.formatTimeWindows(listOf(window), is24HourFormat = true, provider = stringProvider)
        assertEquals("Cả ngày", result)
    }

    @Test
    fun getNextTriggerDisplayInfo_disabledReminder_returnsDisabledInfo() {
        val reminder = Reminder(
            id = 1L,
            title = "Test",
            isEnabled = false,
            triggerType = ReminderTriggerType.FIXED_TIME
        )
        val info = ReminderCardFormatter.getNextTriggerDisplayInfo(reminder, is24HourFormat = true, provider = stringProvider)
        assertEquals("Đã tắt", info.text)
        assertTrue(info.isInactive)
    }

    @Test
    fun getNextTriggerDisplayInfo_snoozedReminder_returnsSnoozeCountdown() {
        val now = LocalDateTime.of(2026, 10, 2, 9, 0)
        val snoozeTime = LocalDateTime.of(2026, 10, 2, 9, 15)
        val snoozeEpoch = snoozeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val reminder = Reminder(
            id = 1L,
            title = "Test",
            isEnabled = true,
            triggerType = ReminderTriggerType.FIXED_TIME,
            snoozeNextTriggerTime = snoozeEpoch
        )

        val info = ReminderCardFormatter.getNextTriggerDisplayInfo(reminder, is24HourFormat = true, provider = stringProvider, now = now)
        assertEquals("Báo lại: 09:15 (còn 15 phút)", info.text)
        assertTrue(info.isSnoozed)
        assertFalse(info.isInactive)
    }

    @Test
    fun getNextTriggerDisplayInfo_todayFutureTrigger_returnsTodayFormat() {
        val now = LocalDateTime.of(2026, 10, 2, 9, 0)
        val trigger = TimeTrigger(
            time = listOf(LocalTime.of(14, 0)),
            repeatMode = RepeatMode.DAILY
        )
        val reminder = Reminder(
            id = 1L,
            title = "Daily reminder",
            isEnabled = true,
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = trigger
        )

        val info = ReminderCardFormatter.getNextTriggerDisplayInfo(reminder, is24HourFormat = true, provider = stringProvider, now = now)
        assertEquals("Lần tới: Hôm nay 14:00 (sau 5 giờ)", info.text)
        assertFalse(info.isSnoozed)
        assertFalse(info.isInactive)
    }

    @Test
    fun getNextTriggerDisplayInfo_tomorrowTrigger_returnsTomorrowFormat() {
        val now = LocalDateTime.of(2026, 10, 2, 20, 0)
        val trigger = TimeTrigger(
            time = listOf(LocalTime.of(8, 0)),
            repeatMode = RepeatMode.DAILY
        )
        val reminder = Reminder(
            id = 1L,
            title = "Daily morning reminder",
            isEnabled = true,
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = trigger
        )

        val info = ReminderCardFormatter.getNextTriggerDisplayInfo(reminder, is24HourFormat = true, provider = stringProvider, now = now)
        assertEquals("Lần tới: Ngày mai 08:00 (sau 12 giờ)", info.text)
        assertFalse(info.isSnoozed)
        assertFalse(info.isInactive)
    }
}
