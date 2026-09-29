package vn.io.litever.remind.core.model

import java.io.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.Year
import java.time.temporal.ChronoUnit

data class TimeTrigger(
    val time: List<LocalTime> = emptyList(), // For FIXED_TIME
    val intervalMinutes: Int? = null, // For INTERVAL
    val exactDate: LocalDate? = null, // Only for RepeatMode.NONE
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val repeatValues: List<Int> = emptyList(), // DayOfWeek values for WEEKLY (1..7) or fallback days
    val monthlyDays: List<Int> = emptyList(), // Days of month for MONTHLY (1..31)
    val yearlyDates: List<ReminderMonthDay> = emptyList(), // Month-Day list for YEARLY
    val timeWindows: List<ReminderTimeWindow> = emptyList() // Multiple TimeWindows for INTERVAL
) : Serializable {

    // Backward-compatible constructor for single timeWindow
    constructor(
        time: List<LocalTime> = emptyList(),
        intervalMinutes: Int? = null,
        exactDate: LocalDate? = null,
        repeatMode: RepeatMode = RepeatMode.NONE,
        repeatValues: List<Int> = emptyList(),
        timeWindow: ReminderTimeWindow? = null
    ) : this(
        time = time,
        intervalMinutes = intervalMinutes,
        exactDate = exactDate,
        repeatMode = repeatMode,
        repeatValues = repeatValues,
        monthlyDays = if (repeatMode == RepeatMode.MONTHLY) repeatValues else emptyList(),
        yearlyDates = emptyList(),
        timeWindows = if (timeWindow != null) listOf(timeWindow) else emptyList()
    )

    val timeWindow: ReminderTimeWindow?
        get() = timeWindows.firstOrNull()

    val isRecurring: Boolean
        get() = repeatMode != RepeatMode.NONE || intervalMinutes != null || monthlyDays.isNotEmpty() || yearlyDates.isNotEmpty() || repeatValues.isNotEmpty()

    /**
     * @param now Current time evaluation point
     * @param lastTriggeredTime The time it was last triggered/acknowledged (critical for INTERVAL)
     * @param isInterval If this trigger is acting as an INTERVAL trigger
     */
    fun getNextOccurrence(
        now: LocalDateTime = LocalDateTime.now(),
        lastTriggeredTime: LocalDateTime? = null,
        isInterval: Boolean = false
    ): LocalDateTime? {
        if (isInterval && intervalMinutes != null) {
            return getNextIntervalOccurrence(now, lastTriggeredTime)
        }
        return getNextFixedTimeOccurrence(now)
    }

    private fun getNextIntervalOccurrence(now: LocalDateTime, lastTriggeredTime: LocalDateTime?): LocalDateTime {
        val interval = (intervalMinutes ?: 1).coerceAtLeast(1)
        val baseTime = lastTriggeredTime ?: now
        var nextTime = baseTime.plusMinutes(interval.toLong())

        // If it's in the past relative to now, fast forward it to the next valid interval after now
        if (nextTime.isBefore(now)) {
            val minutesBetween = ChronoUnit.MINUTES.between(baseTime, now)
            val intervalsToAdd = (minutesBetween / interval) + 1
            nextTime = baseTime.plusMinutes(intervalsToAdd * interval)
        }

        // Apply Multi-TimeWindow constraints if defined
        if (timeWindows.isNotEmpty()) {
            nextTime = adjustForTimeWindows(nextTime, now)
        }

        return nextTime.withSecond(0).withNano(0)
    }

    private fun adjustForTimeWindows(candidateTime: LocalDateTime, now: LocalDateTime): LocalDateTime {
        // If candidate already falls within any active window, it's valid
        if (timeWindows.any { it.isWithinWindow(candidateTime) }) {
            return candidateTime
        }

        val sortedWindows = timeWindows.sortedBy { it.startTime }
        val candidateDate = candidateTime.toLocalDate()
        val candidateLocalTime = candidateTime.toLocalTime()

        // 1. Check later windows on candidateDate
        for (w in sortedWindows) {
            val isDayActive = w.activeDays.isEmpty() || w.activeDays.contains(candidateDate.dayOfWeek)
            if (isDayActive && w.startTime.isAfter(candidateLocalTime)) {
                val shifted = candidateDate.atTime(w.startTime)
                if (!shifted.isBefore(now)) {
                    return shifted
                }
            }
        }

        // 2. Scan upcoming days (up to 7 days ahead) for the next active window
        for (dayOffset in 1..7) {
            val nextDate = candidateDate.plusDays(dayOffset.toLong())
            for (w in sortedWindows) {
                val isDayActive = w.activeDays.isEmpty() || w.activeDays.contains(nextDate.dayOfWeek)
                if (isDayActive) {
                    val shifted = nextDate.atTime(w.startTime)
                    if (!shifted.isBefore(now)) {
                        return shifted
                    }
                }
            }
        }

        // Fallback: next day at earliest window startTime
        val fallbackDate = if (candidateLocalTime.isAfter(sortedWindows.last().endTime)) {
            candidateDate.plusDays(1)
        } else {
            candidateDate
        }
        var fallbackTime = fallbackDate.atTime(sortedWindows.first().startTime)
        if (fallbackTime.isBefore(now)) {
            fallbackTime = fallbackTime.plusDays(1)
        }
        return fallbackTime
    }

    private fun getNextFixedTimeOccurrence(now: LocalDateTime): LocalDateTime? {
        if (time.isEmpty()) return null

        val sortedTimes = time.sorted()

        // 1. NONE: exact date or today/tomorrow
        if (repeatMode == RepeatMode.NONE) {
            val date = exactDate ?: now.toLocalDate()
            for (t in sortedTimes) {
                val candidate = date.atTime(t)
                if (candidate.isAfter(now)) {
                    return candidate.withSecond(0).withNano(0)
                }
            }
            if (exactDate == null) {
                return now.toLocalDate().plusDays(1).atTime(sortedTimes.first()).withSecond(0).withNano(0)
            }
            return null
        }

        // 2. DAILY: repeats every day
        if (repeatMode == RepeatMode.DAILY) {
            for (t in sortedTimes) {
                val candidate = now.toLocalDate().atTime(t)
                if (candidate.isAfter(now)) {
                    return candidate.withSecond(0).withNano(0)
                }
            }
            return now.toLocalDate().plusDays(1).atTime(sortedTimes.first()).withSecond(0).withNano(0)
        }

        // 3. WEEKLY: repeats on selected days of the week (1..7)
        if (repeatMode == RepeatMode.WEEKLY && repeatValues.isNotEmpty()) {
            val todayValue = now.dayOfWeek.value
            val sortedDays = repeatValues.sorted()

            // Try today if active
            if (sortedDays.contains(todayValue)) {
                for (t in sortedTimes) {
                    val candidate = now.toLocalDate().atTime(t)
                    if (candidate.isAfter(now)) {
                        return candidate.withSecond(0).withNano(0)
                    }
                }
            }

            // Find next day in week
            val nextDay = sortedDays.firstOrNull { it > todayValue } ?: sortedDays.first()
            val daysToAdd = if (nextDay > todayValue) nextDay - todayValue else 7 - todayValue + nextDay

            return now.toLocalDate().plusDays(daysToAdd.toLong()).atTime(sortedTimes.first()).withSecond(0).withNano(0)
        }

        // 4. MONTHLY: repeats on selected days of the month (e.g. 1, 15, 30)
        val effectiveMonthlyDays = if (monthlyDays.isNotEmpty()) monthlyDays else repeatValues
        if (repeatMode == RepeatMode.MONTHLY && effectiveMonthlyDays.isNotEmpty()) {
            // Check candidates this month
            val thisMonthCandidates = mutableListOf<LocalDateTime>()
            val today = now.toLocalDate()
            val maxDaysThisMonth = today.lengthOfMonth()

            for (mDay in effectiveMonthlyDays) {
                val validDay = mDay.coerceIn(1, maxDaysThisMonth)
                val targetDate = today.withDayOfMonth(validDay)
                for (t in sortedTimes) {
                    val candidate = targetDate.atTime(t)
                    if (candidate.isAfter(now)) {
                        thisMonthCandidates.add(candidate)
                    }
                }
            }

            if (thisMonthCandidates.isNotEmpty()) {
                return thisMonthCandidates.minOrNull()?.withSecond(0)?.withNano(0)
            }

            // Roll over to next month(s)
            for (monthOffset in 1..12) {
                val futureMonth = today.plusMonths(monthOffset.toLong())
                val maxDaysFutureMonth = futureMonth.lengthOfMonth()
                val futureCandidates = mutableListOf<LocalDateTime>()

                for (mDay in effectiveMonthlyDays) {
                    val validDay = mDay.coerceIn(1, maxDaysFutureMonth)
                    val targetDate = futureMonth.withDayOfMonth(validDay)
                    for (t in sortedTimes) {
                        val candidate = targetDate.atTime(t)
                        if (candidate.isAfter(now)) {
                            futureCandidates.add(candidate)
                        }
                    }
                }

                if (futureCandidates.isNotEmpty()) {
                    return futureCandidates.minOrNull()?.withSecond(0)?.withNano(0)
                }
            }
        }

        // 5. YEARLY: repeats on selected month-day dates (e.g. 8/3, 20/10)
        if (repeatMode == RepeatMode.YEARLY && yearlyDates.isNotEmpty()) {
            // Check this year
            val thisYear = now.year
            val thisYearCandidates = mutableListOf<LocalDateTime>()

            for (ym in yearlyDates) {
                val month = ym.month.coerceIn(1, 12)
                val maxDaysThisYear = Month.of(month).length(Year.isLeap(thisYear.toLong()))
                val day = ym.day.coerceIn(1, maxDaysThisYear)
                val targetDate = LocalDate.of(thisYear, month, day)

                for (t in sortedTimes) {
                    val candidate = targetDate.atTime(t)
                    if (candidate.isAfter(now)) {
                        thisYearCandidates.add(candidate)
                    }
                }
            }

            if (thisYearCandidates.isNotEmpty()) {
                return thisYearCandidates.minOrNull()?.withSecond(0)?.withNano(0)
            }

            // Next year candidates
            val nextYear = thisYear + 1
            val nextYearCandidates = mutableListOf<LocalDateTime>()
            for (ym in yearlyDates) {
                val month = ym.month.coerceIn(1, 12)
                val maxDaysNextYear = Month.of(month).length(Year.isLeap(nextYear.toLong()))
                val day = ym.day.coerceIn(1, maxDaysNextYear)
                val targetDate = LocalDate.of(nextYear, month, day)

                for (t in sortedTimes) {
                    nextYearCandidates.add(targetDate.atTime(t))
                }
            }

            return nextYearCandidates.minOrNull()?.withSecond(0)?.withNano(0)
        }

        // Fallback: check today or roll to tomorrow
        for (t in sortedTimes) {
            val candidate = now.toLocalDate().atTime(t)
            if (candidate.isAfter(now)) {
                return candidate.withSecond(0).withNano(0)
            }
        }
        return now.toLocalDate().plusDays(1).atTime(sortedTimes.first()).withSecond(0).withNano(0)
    }
}
