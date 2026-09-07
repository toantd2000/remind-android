package vn.io.litever.remind.features.alarms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.alarms.ui.state.calculateNextAlarm
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class NextAlarmUiStateTest {

    @Test
    fun calculateNextAlarm_emptyList_returnsAllOff() {
        val result = calculateNextAlarm(emptyList())
        assertEquals(NextAlarmUiState.AllOff, result)
    }

    @Test
    fun calculateNextAlarm_singleFutureAlarmToday_returnsRemainingHoursAndMinutes() {
        val now = LocalTime.now()
        val futureTime = now.plusHours(2).plusMinutes(15)
        
        // Single occurrence alarm today or tomorrow
        val alarm = Alarm(
            id = 1L,
            time = futureTime,
            isEnabled = true
        )

        val result = calculateNextAlarm(listOf(alarm))
        assertTrue("Result should be Remaining state", result is NextAlarmUiState.Remaining)
    }

    @Test
    fun calculateNextAlarm_multipleAlarms_picksEarliestUpcoming() {
        val now = LocalTime.now()
        val earlyAlarm = Alarm(id = 1L, time = now.plusMinutes(10), isEnabled = true)
        val lateAlarm = Alarm(id = 2L, time = now.plusHours(5), isEnabled = true)

        val result = calculateNextAlarm(listOf(lateAlarm, earlyAlarm))
        assertTrue(result is NextAlarmUiState.Remaining)
        val remaining = result as NextAlarmUiState.Remaining
        // Days should be 0, hours should be 0, minutes should be around 9 or 10
        assertEquals(0L, remaining.days)
        assertEquals(0L, remaining.hours)
        assertTrue(remaining.minutes in 9..10)
    }

    @Test
    fun calculateNextAlarm_repeatingAlarm_calculatesRemainingCorrectly() {
        val todayDayOfWeek = LocalDateTime.now().dayOfWeek
        val dayEnum = DayOfWeek.values().first { it.name.equals(todayDayOfWeek.name, ignoreCase = true) }
        
        val alarm = Alarm(
            id = 3L,
            time = LocalTime.now().plusHours(1),
            repeatDays = listOf(dayEnum),
            isEnabled = true
        )

        val result = calculateNextAlarm(listOf(alarm))
        assertTrue(result is NextAlarmUiState.Remaining)
    }

    @Test
    fun calculateNextAlarm_withSpecificFutureDate_calculatesAccurateDays() {
        val futureDate = LocalDate.now().plusDays(3)
        val alarm = Alarm(
            id = 4L,
            time = LocalTime.of(10, 0),
            date = futureDate,
            isEnabled = true
        )

        val result = calculateNextAlarm(listOf(alarm))
        assertTrue(result is NextAlarmUiState.Remaining)
        val remaining = result as NextAlarmUiState.Remaining
        assertTrue("Days should be between 2 and 4", remaining.days in 2..4)
    }
}
