package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure evaluation helper for test assertions and verification.
 */
object ReminderTriggerEvaluatorHelper {

    fun calculateHaversineDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    fun sanitizeSsid(rawSsid: String?): String {
        if (rawSsid == null) return ""
        val trimmed = rawSsid.trim()
        return if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
            trimmed.substring(1, trimmed.length - 1)
        } else {
            trimmed
        }
    }

    fun shouldTriggerLocation(
        reminder: Reminder,
        currentLat: Double,
        currentLon: Double,
        transition: GeofenceTransitionType,
        now: LocalDateTime
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

    fun shouldTriggerWifi(
        reminder: Reminder,
        connectedSsid: String?,
        connectedBssid: String?,
        event: WifiTriggerType,
        now: LocalDateTime
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
}

/**
 * Comprehensive Unit Tests for Trigger Evaluation (Location, Wi-Fi, Time, Time Window, Sanitization) (Tiers 1 & 2).
 */
class ReminderTriggerEvaluatorTest {

    @Test
    fun haversineDistance_accurateForKnownCoordinates() {
        // Hanoi Opera House (21.0244, 105.8573) to Hoan Kiem Lake (21.0285, 105.8542) ~ 560 meters
        val dist = ReminderTriggerEvaluatorHelper.calculateHaversineDistanceMeters(
            21.0244, 105.8573,
            21.0285, 105.8542
        )
        assertTrue("Distance should be approximately 560m, was $dist", dist in 500.0..650.0)
    }

    @Test
    fun locationTrigger_insideRadius_transitionEnter_triggers() {
        val reminder = Reminder(
            id = 1,
            title = "Arrive at office",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.ENTER
            )
        )
        val now = LocalDateTime.of(2026, 8, 19, 9, 0)

        // Point 20 meters away (inside 100m radius)
        val trigger = ReminderTriggerEvaluatorHelper.shouldTriggerLocation(
            reminder = reminder,
            currentLat = 21.0286,
            currentLon = 105.8542,
            transition = GeofenceTransitionType.ENTER,
            now = now
        )
        assertTrue(trigger)
    }

    @Test
    fun locationTrigger_outsideRadius_doesNotTrigger() {
        val reminder = Reminder(
            id = 1,
            title = "Arrive at office",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 50f,
                transitionType = GeofenceTransitionType.ENTER
            )
        )
        val now = LocalDateTime.of(2026, 8, 19, 9, 0)

        // Point 500 meters away
        val trigger = ReminderTriggerEvaluatorHelper.shouldTriggerLocation(
            reminder = reminder,
            currentLat = 21.0244,
            currentLon = 105.8573,
            transition = GeofenceTransitionType.ENTER,
            now = now
        )
        assertFalse(trigger)
    }

    @Test
    fun locationTrigger_transitionMismatch_doesNotTrigger() {
        val reminder = Reminder(
            id = 1,
            title = "Leave office",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.EXIT
            )
        )
        val now = LocalDateTime.of(2026, 8, 19, 9, 0)

        // Event is ENTER, but reminder is configured for EXIT
        val trigger = ReminderTriggerEvaluatorHelper.shouldTriggerLocation(
            reminder = reminder,
            currentLat = 21.0285,
            currentLon = 105.8542,
            transition = GeofenceTransitionType.ENTER,
            now = now
        )
        assertFalse(trigger)
    }

    @Test
    fun locationTrigger_withTimeWindow_insideWindow_triggers() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0)
        )
        val reminder = Reminder(
            id = 1,
            title = "Morning arrival",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.ENTER,
                timeWindow = window
            )
        )
        val insideTime = LocalDateTime.of(2026, 8, 19, 8, 30)

        val trigger = ReminderTriggerEvaluatorHelper.shouldTriggerLocation(
            reminder = reminder,
            currentLat = 21.0285,
            currentLon = 105.8542,
            transition = GeofenceTransitionType.ENTER,
            now = insideTime
        )
        assertTrue(trigger)
    }

    @Test
    fun locationTrigger_withTimeWindow_outsideWindow_doesNotTrigger() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(10, 0)
        )
        val reminder = Reminder(
            id = 1,
            title = "Morning arrival",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.ENTER,
                timeWindow = window
            )
        )
        val outsideTime = LocalDateTime.of(2026, 8, 19, 14, 0) // 14:00 is outside 08:00-10:00

        val trigger = ReminderTriggerEvaluatorHelper.shouldTriggerLocation(
            reminder = reminder,
            currentLat = 21.0285,
            currentLon = 105.8542,
            transition = GeofenceTransitionType.ENTER,
            now = outsideTime
        )
        assertFalse(trigger)
    }

    @Test
    fun wifiTrigger_quotedSsidSanitization_matchesCorrectly() {
        assertEquals("Home_Network", ReminderTriggerEvaluatorHelper.sanitizeSsid("\"Home_Network\""))
        assertEquals("Home_Network", ReminderTriggerEvaluatorHelper.sanitizeSsid("Home_Network"))
        assertEquals("", ReminderTriggerEvaluatorHelper.sanitizeSsid("\"\""))
        assertEquals("", ReminderTriggerEvaluatorHelper.sanitizeSsid(null))
    }

    @Test
    fun wifiTrigger_onConnect_matchesQuotedSystemSsid() {
        val reminder = Reminder(
            id = 2,
            title = "Connect to office wifi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "Office_Secure_5G",
                triggerType = WifiTriggerType.ON_CONNECT
            )
        )
        val now = LocalDateTime.of(2026, 8, 19, 9, 0)

        // System returns quoted SSID: "\"Office_Secure_5G\""
        val trigger = ReminderTriggerEvaluatorHelper.shouldTriggerWifi(
            reminder = reminder,
            connectedSsid = "\"Office_Secure_5G\"",
            connectedBssid = null,
            event = WifiTriggerType.ON_CONNECT,
            now = now
        )
        assertTrue(trigger)
    }

    @Test
    fun wifiTrigger_onDisconnect_triggersWhenDisconnectEventOccurs() {
        val reminder = Reminder(
            id = 2,
            title = "Leaving Home Wi-Fi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "Home_WiFi",
                triggerType = WifiTriggerType.ON_DISCONNECT
            )
        )
        val now = LocalDateTime.of(2026, 8, 19, 8, 0)

        val trigger = ReminderTriggerEvaluatorHelper.shouldTriggerWifi(
            reminder = reminder,
            connectedSsid = "Home_WiFi",
            connectedBssid = null,
            event = WifiTriggerType.ON_DISCONNECT,
            now = now
        )
        assertTrue(trigger)
    }

    @Test
    fun wifiTrigger_withTimeWindow_gatedProperly() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(18, 0),
            endTime = LocalTime.of(23, 0)
        )
        val reminder = Reminder(
            id = 2,
            title = "Evening Home Wi-Fi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "Home_WiFi",
                triggerType = WifiTriggerType.ON_CONNECT,
                timeWindow = window
            )
        )

        val eveningTime = LocalDateTime.of(2026, 8, 19, 19, 30)
        val morningTime = LocalDateTime.of(2026, 8, 19, 9, 30)

        assertTrue(
            ReminderTriggerEvaluatorHelper.shouldTriggerWifi(
                reminder, "Home_WiFi", null, WifiTriggerType.ON_CONNECT, eveningTime
            )
        )
        assertFalse(
            ReminderTriggerEvaluatorHelper.shouldTriggerWifi(
                reminder, "Home_WiFi", null, WifiTriggerType.ON_CONNECT, morningTime
            )
        )
    }

    @Test
    fun disabledOrCompletedReminder_neverTriggers() {
        val disabledReminder = Reminder(
            id = 3,
            title = "Disabled",
            isEnabled = false,
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 0.0, longitude = 0.0, radiusMeters = 1000f)
        )
        val completedReminder = Reminder(
            id = 4,
            title = "Completed",
            isCompleted = true,
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 0.0, longitude = 0.0, radiusMeters = 1000f)
        )
        val now = LocalDateTime.of(2026, 8, 19, 12, 0)

        assertFalse(
            ReminderTriggerEvaluatorHelper.shouldTriggerLocation(
                disabledReminder, 0.0, 0.0, GeofenceTransitionType.ENTER, now
            )
        )
        assertFalse(
            ReminderTriggerEvaluatorHelper.shouldTriggerLocation(
                completedReminder, 0.0, 0.0, GeofenceTransitionType.ENTER, now
            )
        )
    }
}
