package vn.io.litever.remind.core.alarm.reminder

import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDateTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Result data class produced by trigger evaluation.
 */
data class ReminderEvaluationResult(
    val reminder: Reminder,
    val isTriggered: Boolean,
    val isInsideGeofence: Boolean? = null,
    val isConnectedWifi: Boolean? = null,
    val distanceMeters: Double? = null,
    val reason: String = ""
)

/**
 * Pure Kotlin evaluation engine for Location, Wi-Fi, and Time Window triggers.
 * 100% JVM testable with zero Android framework dependencies.
 */
object ReminderTriggerEvaluator {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates the great-circle distance between two geographic coordinates using the Haversine formula.
     */
    fun calculateHaversineDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Normalizes Wi-Fi SSID strings by stripping enclosing quotes and trimming.
     * Handles Android's WifiInfo quirks (e.g. "\"Home_WiFi\"", "<unknown ssid>").
     */
    fun sanitizeSsid(rawSsid: String?): String {
        if (rawSsid == null) return ""
        val trimmed = rawSsid.trim()
        val clean = if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
            trimmed.substring(1, trimmed.length - 1)
        } else {
            trimmed
        }
        return if (clean == "<unknown ssid>") "" else clean
    }

    /**
     * Evaluates whether a given time is within a ReminderTimeWindow.
     */
    fun isWithinTimeWindow(window: ReminderTimeWindow?, dateTime: LocalDateTime): Boolean {
        if (window == null) return true
        return window.isWithinWindow(dateTime)
    }

    /**
     * Determines whether a location reminder should trigger for the given coordinates and transition event.
     */
    fun shouldTriggerLocation(
        reminder: Reminder,
        currentLat: Double,
        currentLon: Double,
        transition: GeofenceTransitionType,
        now: LocalDateTime = LocalDateTime.now()
    ): Boolean {
        if (!reminder.isEnabled || reminder.isCompleted) return false
        val locTrigger = reminder.locationTrigger ?: return false

        // Check transition match
        val matchesTransition = locTrigger.transitionType == GeofenceTransitionType.BOTH ||
                locTrigger.transitionType == transition
        if (!matchesTransition) return false

        // Check distance
        val distance = calculateHaversineDistanceMeters(
            locTrigger.latitude, locTrigger.longitude,
            currentLat, currentLon
        )
        if (distance > locTrigger.radiusMeters) return false

        // Check time window if present
        val timeWindow = locTrigger.timeWindow
        if (timeWindow != null && !timeWindow.isWithinWindow(now)) {
            return false
        }

        return true
    }

    /**
     * Determines whether a Wi-Fi reminder should trigger for the given SSID, BSSID, and event.
     */
    fun shouldTriggerWifi(
        reminder: Reminder,
        connectedSsid: String?,
        connectedBssid: String?,
        event: WifiTriggerType,
        now: LocalDateTime = LocalDateTime.now()
    ): Boolean {
        if (!reminder.isEnabled || reminder.isCompleted) return false
        val wifiTrigger = reminder.wifiTrigger ?: return false

        val targetSsid = sanitizeSsid(wifiTrigger.ssid)
        val actualSsid = sanitizeSsid(connectedSsid)

        if (targetSsid.isEmpty() || !targetSsid.equals(actualSsid, ignoreCase = true)) {
            return false
        }

        if (wifiTrigger.bssid != null && !wifiTrigger.bssid.equals(connectedBssid, ignoreCase = true)) {
            return false
        }

        if (wifiTrigger.triggerType != event && wifiTrigger.triggerType != WifiTriggerType.WHILE_CONNECTED) {
            return false
        }

        val timeWindow = wifiTrigger.timeWindow
        if (timeWindow != null && !timeWindow.isWithinWindow(now)) {
            return false
        }

        return true
    }

    /**
     * Evaluates a single location reminder against current location and previous inside/outside state.
     */
    fun evaluateLocationReminder(
        reminder: Reminder,
        currentLatitude: Double,
        currentLongitude: Double,
        wasInside: Boolean?,
        currentDateTime: LocalDateTime = LocalDateTime.now()
    ): ReminderEvaluationResult {
        if (!reminder.isEnabled || reminder.isCompleted || reminder.triggerType != ReminderTriggerType.LOCATION) {
            return ReminderEvaluationResult(reminder, isTriggered = false, reason = "Disabled or completed")
        }

        val locationTrigger = reminder.locationTrigger
            ?: return ReminderEvaluationResult(reminder, isTriggered = false, reason = "No location trigger configured")

        val distance = calculateHaversineDistanceMeters(
            locationTrigger.latitude, locationTrigger.longitude,
            currentLatitude, currentLongitude
        )
        val isCurrentlyInside = distance <= locationTrigger.radiusMeters

        // Detect transition
        val hasTransition = when (locationTrigger.transitionType) {
            GeofenceTransitionType.ENTER -> {
                (wasInside == false || wasInside == null) && isCurrentlyInside
            }
            GeofenceTransitionType.EXIT -> {
                wasInside == true && !isCurrentlyInside
            }
            GeofenceTransitionType.BOTH -> {
                if (wasInside == null) {
                    isCurrentlyInside
                } else {
                    wasInside != isCurrentlyInside
                }
            }
        }

        if (!hasTransition) {
            return ReminderEvaluationResult(
                reminder = reminder,
                isTriggered = false,
                isInsideGeofence = isCurrentlyInside,
                distanceMeters = distance,
                reason = "No matching geofence transition detected"
            )
        }

        // Check time window
        val isWithinWindow = isWithinTimeWindow(locationTrigger.timeWindow, currentDateTime)
        if (!isWithinWindow) {
            return ReminderEvaluationResult(
                reminder = reminder,
                isTriggered = false,
                isInsideGeofence = isCurrentlyInside,
                distanceMeters = distance,
                reason = "Transition matched but outside active time window"
            )
        }

        return ReminderEvaluationResult(
            reminder = reminder,
            isTriggered = true,
            isInsideGeofence = isCurrentlyInside,
            distanceMeters = distance,
            reason = "Geofence trigger activated"
        )
    }

    /**
     * Evaluates a single Wi-Fi reminder against current Wi-Fi connection and previous connected state.
     */
    fun evaluateWifiReminder(
        reminder: Reminder,
        currentSsid: String?,
        currentBssid: String?,
        isConnected: Boolean,
        wasConnected: Boolean?,
        currentDateTime: LocalDateTime = LocalDateTime.now()
    ): ReminderEvaluationResult {
        if (!reminder.isEnabled || reminder.isCompleted || reminder.triggerType != ReminderTriggerType.WIFI) {
            return ReminderEvaluationResult(reminder, isTriggered = false, reason = "Disabled or completed")
        }

        val wifiTrigger = reminder.wifiTrigger
            ?: return ReminderEvaluationResult(reminder, isTriggered = false, reason = "No Wi-Fi trigger configured")

        val targetSsid = sanitizeSsid(wifiTrigger.ssid)
        val actualSsid = sanitizeSsid(currentSsid)

        val isSsidMatch = targetSsid.isNotEmpty() && targetSsid.equals(actualSsid, ignoreCase = true)
        val isBssidMatch = wifiTrigger.bssid.isNullOrBlank() || wifiTrigger.bssid.equals(currentBssid, ignoreCase = true)
        val isTargetNetwork = isConnected && isSsidMatch && isBssidMatch

        // Detect transition
        val hasTransition = when (wifiTrigger.triggerType) {
            WifiTriggerType.ON_CONNECT -> {
                (wasConnected == false || wasConnected == null) && isTargetNetwork
            }
            WifiTriggerType.ON_DISCONNECT -> {
                wasConnected == true && !isTargetNetwork
            }
            WifiTriggerType.WHILE_CONNECTED -> {
                isTargetNetwork
            }
        }

        if (!hasTransition) {
            return ReminderEvaluationResult(
                reminder = reminder,
                isTriggered = false,
                isConnectedWifi = isTargetNetwork,
                reason = "No matching Wi-Fi transition detected"
            )
        }

        // Check time window
        val isWithinWindow = isWithinTimeWindow(wifiTrigger.timeWindow, currentDateTime)
        if (!isWithinWindow) {
            return ReminderEvaluationResult(
                reminder = reminder,
                isTriggered = false,
                isConnectedWifi = isTargetNetwork,
                reason = "Wi-Fi matched but outside active time window"
            )
        }

        return ReminderEvaluationResult(
            reminder = reminder,
            isTriggered = true,
            isConnectedWifi = isTargetNetwork,
            reason = "Wi-Fi trigger activated"
        )
    }

    /**
     * Batch evaluator for location reminders monitored by ReminderMonitorService.
     */
    fun evaluateLocationReminders(
        reminders: List<Reminder>,
        currentLatitude: Double,
        currentLongitude: Double,
        previousInsideStates: Map<Long, Boolean>,
        currentDateTime: LocalDateTime = LocalDateTime.now()
    ): List<ReminderEvaluationResult> {
        return reminders.map { reminder ->
            evaluateLocationReminder(
                reminder = reminder,
                currentLatitude = currentLatitude,
                currentLongitude = currentLongitude,
                wasInside = previousInsideStates[reminder.id],
                currentDateTime = currentDateTime
            )
        }
    }

    /**
     * Batch evaluator for Wi-Fi reminders monitored by ReminderMonitorService.
     */
    fun evaluateWifiReminders(
        reminders: List<Reminder>,
        currentSsid: String?,
        currentBssid: String?,
        isConnected: Boolean,
        previousConnectedStates: Map<Long, Boolean>,
        currentDateTime: LocalDateTime = LocalDateTime.now()
    ): List<ReminderEvaluationResult> {
        return reminders.map { reminder ->
            evaluateWifiReminder(
                reminder = reminder,
                currentSsid = currentSsid,
                currentBssid = currentBssid,
                isConnected = isConnected,
                wasConnected = previousConnectedStates[reminder.id],
                currentDateTime = currentDateTime
            )
        }
    }
}
