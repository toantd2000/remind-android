package vn.io.litever.remind.core.model

import java.io.Serializable
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
    val exactDates: List<LocalDate> = if (exactDate != null) listOf(exactDate) else emptyList(), // Multiple exact dates for RepeatMode.NONE
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
        exactDates = if (exactDate != null) listOf(exactDate) else emptyList(),
        repeatMode = repeatMode,
        repeatValues = repeatValues,
        monthlyDays = if (repeatMode == RepeatMode.MONTHLY) repeatValues else emptyList(),
        yearlyDates = emptyList(),
        timeWindows = if (timeWindow != null) listOf(timeWindow) else emptyList()
    )

    val timeWindow: ReminderTimeWindow?
        get() = timeWindows.firstOrNull()

    val effectiveExactDates: List<LocalDate>
        get() = (if (exactDates.isNotEmpty()) exactDates else listOfNotNull(exactDate)).distinct().sorted()

    val isRecurring: Boolean
        get() = repeatMode != RepeatMode.NONE || (effectiveExactDates.isEmpty() && intervalMinutes != null)

    /**
     * Checks whether a specific date satisfies the recurrence condition.
     */
    fun isDateValid(date: LocalDate): Boolean {
        return when (repeatMode) {
            RepeatMode.NONE -> {
                val targets = effectiveExactDates
                if (targets.isNotEmpty()) {
                    targets.contains(date)
                } else {
                    true
                }
            }
            RepeatMode.DAILY -> true
            RepeatMode.WEEKLY -> {
                if (repeatValues.isEmpty()) true
                else repeatValues.contains(date.dayOfWeek.value)
            }
            RepeatMode.MONTHLY -> {
                val days = if (monthlyDays.isNotEmpty()) monthlyDays else repeatValues
                if (days.isEmpty()) true
                else {
                    val maxDays = date.lengthOfMonth()
                    days.any { day ->
                        val validDay = day.coerceIn(1, maxDays)
                        date.dayOfMonth == validDay
                    }
                }
            }
            RepeatMode.YEARLY -> {
                if (yearlyDates.isEmpty()) true
                else yearlyDates.any { it.month == date.monthValue && it.day == date.dayOfMonth }
            }
        }
    }

    /**
     * Finds the next valid date on or after [from] that satisfies the recurrence condition.
     */
    fun getNextValidDate(from: LocalDate): LocalDate? {
        return when (repeatMode) {
            RepeatMode.NONE -> {
                val targets = effectiveExactDates
                if (targets.isNotEmpty()) {
                    targets.firstOrNull { !from.isAfter(it) }
                } else {
                    from
                }
            }
            RepeatMode.DAILY -> from
            RepeatMode.WEEKLY -> {
                for (offset in 0..7) {
                    val candidate = from.plusDays(offset.toLong())
                    if (isDateValid(candidate)) return candidate
                }
                null
            }
            RepeatMode.MONTHLY -> {
                for (offset in 0..366) {
                    val candidate = from.plusDays(offset.toLong())
                    if (isDateValid(candidate)) return candidate
                }
                null
            }
            RepeatMode.YEARLY -> {
                for (offset in 0..730) {
                    val candidate = from.plusDays(offset.toLong())
                    if (isDateValid(candidate)) return candidate
                }
                null
            }
        }
    }

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

    private fun getNextIntervalOccurrence(now: LocalDateTime, lastTriggeredTime: LocalDateTime?): LocalDateTime? {
        val interval = (intervalMinutes ?: 1).coerceAtLeast(1)
        val sortedWindows = if (timeWindows.isNotEmpty()) {
            timeWindows.sortedBy { it.startTime }
        } else {
            listOf(ReminderTimeWindow(startTime = LocalTime.of(0, 0), endTime = LocalTime.of(23, 59)))
        }

        val today = now.toLocalDate()
        val targets = effectiveExactDates

        // 1. If exactDates are in the future (today is not in targets) for NONE mode
        if (repeatMode == RepeatMode.NONE && targets.isNotEmpty() && !targets.contains(today)) {
            val nextTarget = targets.firstOrNull { it.isAfter(today) } ?: return null
            val activeWindow = sortedWindows.firstOrNull {
                it.activeDays.isEmpty() || it.activeDays.contains(nextTarget.dayOfWeek)
            } ?: sortedWindows.first()
            return nextTarget.atTime(activeWindow.startTime).withSecond(0).withNano(0)
        }

        // 2. Check if today is a valid date
        if (isDateValid(today)) {
            val baseTime = lastTriggeredTime ?: now
            var nextTime = if (lastTriggeredTime != null) {
                lastTriggeredTime.plusMinutes(interval.toLong())
            } else {
                now.plusMinutes(interval.toLong())
            }

            if (nextTime.isBefore(now)) {
                val minutesBetween = ChronoUnit.MINUTES.between(baseTime, now)
                val intervalsToAdd = (minutesBetween / interval) + 1
                nextTime = baseTime.plusMinutes(intervalsToAdd * interval)
            }

            val candidateDate = nextTime.toLocalDate()
            val candidateLocalTime = nextTime.toLocalTime()

            if (candidateDate == today) {
                for (w in sortedWindows) {
                    val isDayActive = w.activeDays.isEmpty() || w.activeDays.contains(today.dayOfWeek)
                    if (isDayActive) {
                        if (candidateLocalTime in w.startTime..w.endTime) {
                            return nextTime.withSecond(0).withNano(0)
                        } else if (candidateLocalTime.isBefore(w.startTime)) {
                            return today.atTime(w.startTime).withSecond(0).withNano(0)
                        }
                    }
                }
            }
        }

        // 3. Today has no more intervals/windows or is not valid. Find next valid date.
        if (repeatMode == RepeatMode.NONE && targets.isNotEmpty()) {
            val nextTarget = targets.firstOrNull { it.isAfter(today) } ?: return null
            val activeWindow = sortedWindows.firstOrNull {
                it.activeDays.isEmpty() || it.activeDays.contains(nextTarget.dayOfWeek)
            } ?: sortedWindows.first()
            return nextTarget.atTime(activeWindow.startTime).withSecond(0).withNano(0)
        }

        val startNextDate = today.plusDays(1)
        val nextValidDate = getNextValidDate(startNextDate) ?: return null

        for (dayOffset in 0..7) {
            val candidateDate = nextValidDate.plusDays(dayOffset.toLong())
            if (isDateValid(candidateDate)) {
                for (w in sortedWindows) {
                    val isDayActive = w.activeDays.isEmpty() || w.activeDays.contains(candidateDate.dayOfWeek)
                    if (isDayActive) {
                        return candidateDate.atTime(w.startTime).withSecond(0).withNano(0)
                    }
                }
            }
        }

        val earliestWindow = sortedWindows.first()
        return nextValidDate.atTime(earliestWindow.startTime).withSecond(0).withNano(0)
    }

    private fun getNextFixedTimeOccurrence(now: LocalDateTime): LocalDateTime? {
        if (time.isEmpty()) return null

        val sortedTimes = time.sorted()

        // 1. NONE: exact dates or today/tomorrow
        if (repeatMode == RepeatMode.NONE) {
            val targets = effectiveExactDates
            if (targets.isNotEmpty()) {
                val today = now.toLocalDate()
                for (d in targets) {
                    if (!d.isBefore(today)) {
                        for (t in sortedTimes) {
                            val candidate = d.atTime(t)
                            if (candidate.isAfter(now)) {
                                return candidate.withSecond(0).withNano(0)
                            }
                        }
                    }
                }
                return null
            } else {
                val date = now.toLocalDate()
                for (t in sortedTimes) {
                    val candidate = date.atTime(t)
                    if (candidate.isAfter(now)) {
                        return candidate.withSecond(0).withNano(0)
                    }
                }
                return date.plusDays(1).atTime(sortedTimes.first()).withSecond(0).withNano(0)
            }
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
