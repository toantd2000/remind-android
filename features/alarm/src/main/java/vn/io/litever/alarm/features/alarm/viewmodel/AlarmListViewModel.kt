package vn.io.litever.alarm.features.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.alarm.core.domain.repository.AlarmRepository
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.core.model.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

sealed interface NextAlarmUiState {
    object AllOff : NextAlarmUiState
    data class Remaining(
        val days: Long,
        val hours: Long,
        val minutes: Long
    ) : NextAlarmUiState
}

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val nextAlarmTime: StateFlow<NextAlarmUiState> = alarms
        .map { list ->
            calculateNextAlarm(list.filter { it.isEnabled })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NextAlarmUiState.AllOff
        )

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updatedAlarm)
            if (updatedAlarm.isEnabled) {
                alarmScheduler.schedule(updatedAlarm)
            } else {
                alarmScheduler.cancel(updatedAlarm)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            alarmScheduler.cancel(alarm)
        }
    }

    fun deleteDisabledAlarms() {
        viewModelScope.launch {
            val disabledAlarms = alarms.value.filter { !it.isEnabled }
            disabledAlarms.forEach { alarm ->
                repository.deleteAlarm(alarm)
                alarmScheduler.cancel(alarm)
            }
        }
    }

    private fun calculateNextAlarm(enabledAlarms: List<Alarm>): NextAlarmUiState {
        if (enabledAlarms.isEmpty()) return NextAlarmUiState.AllOff
        
        val now = LocalDateTime.now()
        val nextOccurrences = enabledAlarms.mapNotNull { alarm ->
            calculateNextOccurrence(alarm, now)
        }
        
        val earliest = nextOccurrences.minOrNull() ?: return NextAlarmUiState.AllOff
        val duration = Duration.between(now, earliest)
        
        val totalMinutes = duration.toMinutes()
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = totalMinutes % 60
        
        return NextAlarmUiState.Remaining(days, hours, minutes)
    }

    private fun calculateNextOccurrence(alarm: Alarm, now: LocalDateTime): LocalDateTime? {
        val alarmTime = alarm.time
        
        if (alarm.repeatDays.isEmpty()) {
            // One-time alarm
            val todayOccurrence = now.with(alarmTime).withSecond(0).withNano(0)
            return if (todayOccurrence.isAfter(now)) {
                todayOccurrence
            } else {
                todayOccurrence.plusDays(1)
            }
        } else {
            // Repeating alarm
            val todayDayValue = now.dayOfWeek.value // 1 (Mon) to 7 (Sun)
            
            // Map DayOfWeek enum to Java DayOfWeek value
            val repeatDayValues = alarm.repeatDays.map { it.toJavaDayValue() }.sorted()
            
            // 1. Check if it's later today
            if (repeatDayValues.contains(todayDayValue)) {
                val todayOccurrence = now.with(alarmTime).withSecond(0).withNano(0)
                if (todayOccurrence.isAfter(now)) return todayOccurrence
            }
            
            // 2. Check next days in the same week
            val nextDayInWeek = repeatDayValues.firstOrNull { it > todayDayValue }
            if (nextDayInWeek != null) {
                val daysDiff = (nextDayInWeek - todayDayValue).toLong()
                return now.with(alarmTime).plusDays(daysDiff).withSecond(0).withNano(0)
            }
            
            // 3. Check first day in next week
            val firstDayNextWeek = repeatDayValues.first()
            val daysDiff = (7 - todayDayValue + firstDayNextWeek).toLong()
            return now.with(alarmTime).plusDays(daysDiff).withSecond(0).withNano(0)
        }
    }

    private fun DayOfWeek.toJavaDayValue(): Int = when (this) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
        DayOfWeek.SUNDAY -> 7
    }
}
