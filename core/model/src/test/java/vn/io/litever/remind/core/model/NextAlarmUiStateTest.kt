package vn.io.litever.remind.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class NextAlarmUiStateTest {

    @Test
    fun calculateNextAlarm_whenNoAlarms_returnsAllOff() {
        val result = calculateNextAlarm(emptyList())
        assertEquals(NextAlarmUiState.AllOff, result)
    }

    @Test
    fun calculateNextAlarm_whenAllDisabled_returnsAllOff() {
        val alarms = listOf(
            Alarm(time = LocalTime.of(8, 0), isEnabled = false)
        )
        val result = calculateNextAlarm(alarms.filter { it.isEnabled })
        assertEquals(NextAlarmUiState.AllOff, result)
    }

    @Test
    fun calculateNextAlarm_withEnabledAlarm_returnsRemainingWithAlarm() {
        val now = LocalDateTime.now()
        val alarmTime = now.toLocalTime().plusHours(2).plusMinutes(15)
        val alarm = Alarm(
            id = 42L,
            time = alarmTime,
            label = "Test Alarm",
            isEnabled = true
        )

        val result = calculateNextAlarm(listOf(alarm))
        assertTrue(result is NextAlarmUiState.Remaining)
        val remaining = result as NextAlarmUiState.Remaining
        assertTrue(remaining.hours >= 2 || remaining.days > 0)
        assertEquals(42L, remaining.alarm.id)
        assertEquals("Test Alarm", remaining.alarm.label)
    }
}
