package vn.io.litever.remind.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class AlarmTest {

    @Test
    fun isSkipExpired_whenSkippedAtIsNull_returnsFalse() {
        val alarm = Alarm(
            time = LocalTime.of(10, 0),
            skippedAt = null
        )
        assertFalse(alarm.isSkipExpired(LocalDateTime.now()))
    }

    @Test
    fun isSkipExpired_whenSkippedAtIsInFuture_returnsFalse() {
        val now = LocalDateTime.of(2024, 1, 1, 9, 0)
        val skippedAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        val alarm = Alarm(
            time = LocalTime.of(10, 0),
            skippedAt = skippedAt
        )
        assertFalse(alarm.isSkipExpired(now))
    }

    @Test
    fun isSkipExpired_whenSkippedAtIsInPast_returnsTrue() {
        val now = LocalDateTime.of(2024, 1, 1, 11, 0)
        val skippedAt = LocalDateTime.of(2024, 1, 1, 10, 0)
        val alarm = Alarm(
            time = LocalTime.of(10, 0),
            skippedAt = skippedAt
        )
        assertTrue(alarm.isSkipExpired(now))
    }
}
