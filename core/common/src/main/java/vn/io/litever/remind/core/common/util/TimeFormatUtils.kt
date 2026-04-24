package vn.io.litever.remind.core.common.util

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormatUtils {
    private val format24h = DateTimeFormatter.ofPattern("HH:mm")
    private val formatTime12h = DateTimeFormatter.ofPattern("h:mm")
    private val formatAmPm = DateTimeFormatter.ofPattern("a")

    fun formatTime(time: LocalTime, is24Hour: Boolean): String {
        return if (is24Hour) {
            time.format(format24h)
        } else {
            time.format(DateTimeFormatter.ofPattern("h:mm a"))
        }
    }

    fun formatTimeParts(time: LocalTime, is24Hour: Boolean): Pair<String, String?> {
        return if (is24Hour) {
            time.format(format24h) to null
        } else {
            time.format(formatTime12h) to time.format(formatAmPm)
        }
    }

    fun getTimeFormatter(is24Hour: Boolean): DateTimeFormatter {
        return if (is24Hour) format24h else DateTimeFormatter.ofPattern("h:mm a")
    }
}










