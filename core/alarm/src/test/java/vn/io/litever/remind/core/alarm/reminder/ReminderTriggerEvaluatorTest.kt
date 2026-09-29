package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
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

    @Test
    fun evaluateLocationReminder_directEngine_stateTransitions() {
        val reminder = Reminder(
            id = 10,
            title = "Enter Store",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 10.0,
                longitude = 106.0,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.ENTER
            )
        )

        // Was outside, now inside -> triggered!
        val res1 = ReminderTriggerEvaluator.evaluateLocationReminder(
            reminder = reminder,
            currentLatitude = 10.0001,
            currentLongitude = 106.0,
            wasInside = false
        )
        assertTrue(res1.isTriggered)
        assertTrue(res1.isInsideGeofence == true)

        // Was inside, still inside -> NOT triggered (no rising edge)
        val res2 = ReminderTriggerEvaluator.evaluateLocationReminder(
            reminder = reminder,
            currentLatitude = 10.0001,
            currentLongitude = 106.0,
            wasInside = true
        )
        assertFalse(res2.isTriggered)
        assertTrue(res2.isInsideGeofence == true)

        // Was inside, now outside with EXIT trigger -> triggered!
        val exitReminder = reminder.copy(
            locationTrigger = reminder.locationTrigger?.copy(transitionType = GeofenceTransitionType.EXIT)
        )
        val res3 = ReminderTriggerEvaluator.evaluateLocationReminder(
            reminder = exitReminder,
            currentLatitude = 10.1,
            currentLongitude = 106.0,
            wasInside = true
        )
        assertTrue(res3.isTriggered)
        assertFalse(res3.isInsideGeofence == true)
    }

    @Test
    fun evaluateWifiReminder_directEngine_connectAndDisconnect() {
        val onConnectReminder = Reminder(
            id = 20,
            title = "Wifi Connect",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "Cafe_WiFi",
                bssid = "AA:BB:CC:DD:EE:FF",
                triggerType = WifiTriggerType.ON_CONNECT
            )
        )

        // Connects to target SSID and matching BSSID
        val res1 = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = onConnectReminder,
            currentSsid = "\"Cafe_WiFi\"",
            currentBssid = "aa:bb:cc:dd:ee:ff",
            isConnected = true,
            wasConnected = false
        )
        assertTrue(res1.isTriggered)

        // Connects to target SSID but mismatching BSSID
        val res2 = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = onConnectReminder,
            currentSsid = "\"Cafe_WiFi\"",
            currentBssid = "00:11:22:33:44:55",
            isConnected = true,
            wasConnected = false
        )
        assertFalse(res2.isTriggered)

        // On Disconnect
        val onDisconnectReminder = onConnectReminder.copy(
            wifiTrigger = onConnectReminder.wifiTrigger?.copy(triggerType = WifiTriggerType.ON_DISCONNECT, bssid = null)
        )
        val res3 = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = onDisconnectReminder,
            currentSsid = null,
            currentBssid = null,
            isConnected = false,
            wasConnected = true
        )
        assertTrue(res3.isTriggered)
    }

    @Test
    fun batchEvaluations_processMultipleReminders() {
        val reminders = listOf(
            Reminder(
                id = 1,
                title = "Loc1",
                triggerType = ReminderTriggerType.LOCATION,
                locationTrigger = LocationTrigger(latitude = 10.0, longitude = 106.0, radiusMeters = 100f)
            ),
            Reminder(
                id = 2,
                title = "Loc2",
                triggerType = ReminderTriggerType.LOCATION,
                locationTrigger = LocationTrigger(latitude = 20.0, longitude = 105.0, radiusMeters = 100f)
            )
        )

        val results = ReminderTriggerEvaluator.evaluateLocationReminders(
            reminders = reminders,
            currentLatitude = 10.0001,
            currentLongitude = 106.0,
            previousInsideStates = mapOf(1L to false, 2L to false)
        )

        assertEquals(2, results.size)
        assertTrue(results[0].isTriggered)
        assertFalse(results[1].isTriggered)
    }

    @Test
    fun isDateScheduleActive_evaluatesAllRepeatModes() {
        val targetDate = java.time.LocalDate.of(2026, 8, 28) // Friday, Aug 28

        // One-time exact date match
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = java.time.LocalDate.of(2026, 8, 28),
                repeatDays = emptyList(),
                monthlyDays = emptyList(),
                yearlyDates = emptyList(),
                targetDate = targetDate
            )
        )
        // One-time different date
        assertFalse(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = java.time.LocalDate.of(2026, 8, 29),
                repeatDays = emptyList(),
                monthlyDays = emptyList(),
                yearlyDates = emptyList(),
                targetDate = targetDate
            )
        )

        // Weekly repeat matching Friday
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                monthlyDays = emptyList(),
                yearlyDates = emptyList(),
                targetDate = targetDate
            )
        )
        // Weekly repeat not containing Friday
        assertFalse(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                monthlyDays = emptyList(),
                yearlyDates = emptyList(),
                targetDate = targetDate
            )
        )

        // Monthly repeat matching 28th
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = listOf(1, 15, 28),
                yearlyDates = emptyList(),
                targetDate = targetDate
            )
        )
        // Monthly repeat not matching 28th
        assertFalse(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = listOf(1, 15, 30),
                yearlyDates = emptyList(),
                targetDate = targetDate
            )
        )

        // Yearly repeat matching Aug 28 (month=8, day=28)
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = emptyList(),
                yearlyDates = listOf(vn.io.litever.remind.core.model.ReminderMonthDay(8, 28)),
                targetDate = targetDate
            )
        )
        // Yearly repeat not matching Aug 28
        assertFalse(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = emptyList(),
                yearlyDates = listOf(vn.io.litever.remind.core.model.ReminderMonthDay(12, 25)),
                targetDate = targetDate
            )
        )
    }

    @Test
    fun locationTrigger_withWeeklyRepeat_onlyTriggersOnMatchingDays() {
        val reminder = Reminder(
            id = 30,
            title = "Office Arrival Mon/Fri",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.ENTER,
                repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                timeWindows = listOf(
                    ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 0))
                )
            )
        )

        // Friday 2026-08-28 at 09:00 -> Active!
        val friday = LocalDateTime.of(2026, 8, 28, 9, 0)
        val resFriday = ReminderTriggerEvaluator.evaluateLocationReminder(
            reminder = reminder,
            currentLatitude = 21.0285,
            currentLongitude = 105.8542,
            wasInside = false,
            currentDateTime = friday
        )
        assertTrue(resFriday.isTriggered)

        // Thursday 2026-08-27 at 09:00 -> NOT active day!
        val thursday = LocalDateTime.of(2026, 8, 27, 9, 0)
        val resThursday = ReminderTriggerEvaluator.evaluateLocationReminder(
            reminder = reminder,
            currentLatitude = 21.0285,
            currentLongitude = 105.8542,
            wasInside = false,
            currentDateTime = thursday
        )
        assertFalse(resThursday.isTriggered)
    }

    @Test
    fun wifiTrigger_withMonthlyRepeat_onlyTriggersOnMatchingMonthlyDays() {
        val reminder = Reminder(
            id = 40,
            title = "Connect Wifi on 1st & 15th",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "Company_Office",
                triggerType = WifiTriggerType.ON_CONNECT,
                monthlyDays = listOf(1, 15),
                timeWindows = listOf(ReminderTimeWindow.ALL_DAY)
            )
        )

        // 15th of the month -> Active!
        val onFifteenth = LocalDateTime.of(2026, 8, 15, 10, 0)
        val res15 = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "Company_Office",
            currentBssid = null,
            isConnected = true,
            wasConnected = false,
            currentDateTime = onFifteenth
        )
        assertTrue(res15.isTriggered)

        // 16th of the month -> Inactive!
        val onSixteenth = LocalDateTime.of(2026, 8, 16, 10, 0)
        val res16 = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "Company_Office",
            currentBssid = null,
            isConnected = true,
            wasConnected = false,
            currentDateTime = onSixteenth
        )
        assertFalse(res16.isTriggered)
    }

    @Test
    fun isDateScheduleActive_monthlyDay31_clampsOnShorterMonths() {
        // Day 31 recurrence in February 2026 (28 days) -> should be active on Feb 28
        val feb28 = java.time.LocalDate.of(2026, 2, 28)
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = listOf(31),
                yearlyDates = emptyList(),
                targetDate = feb28
            )
        )

        // Day 31 recurrence in February 2026 on Feb 27 -> should NOT be active
        val feb27 = java.time.LocalDate.of(2026, 2, 27)
        assertFalse(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = listOf(31),
                yearlyDates = emptyList(),
                targetDate = feb27
            )
        )

        // Day 31 recurrence in April 2026 (30 days) -> should be active on Apr 30
        val apr30 = java.time.LocalDate.of(2026, 4, 30)
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = listOf(31),
                yearlyDates = emptyList(),
                targetDate = apr30
            )
        )

        // Day 31 recurrence in August 2026 (31 days) -> should be active on Aug 31, but NOT Aug 30
        val aug30 = java.time.LocalDate.of(2026, 8, 30)
        val aug31 = java.time.LocalDate.of(2026, 8, 31)
        assertFalse(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = listOf(31),
                yearlyDates = emptyList(),
                targetDate = aug30
            )
        )
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = listOf(31),
                yearlyDates = emptyList(),
                targetDate = aug31
            )
        )
    }

    @Test
    fun isDateScheduleActive_yearlyLeapDay_handlesNonLeapYears() {
        // Feb 29 yearly reminder in non-leap year (2026) -> active on Feb 28
        val nonLeapFeb28 = java.time.LocalDate.of(2026, 2, 28)
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = emptyList(),
                yearlyDates = listOf(vn.io.litever.remind.core.model.ReminderMonthDay(2, 29)),
                targetDate = nonLeapFeb28
            )
        )

        // Feb 29 yearly reminder in leap year (2024) -> active on Feb 29, NOT Feb 28
        val leapFeb28 = java.time.LocalDate.of(2024, 2, 28)
        val leapFeb29 = java.time.LocalDate.of(2024, 2, 29)
        assertFalse(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = emptyList(),
                yearlyDates = listOf(vn.io.litever.remind.core.model.ReminderMonthDay(2, 29)),
                targetDate = leapFeb28
            )
        )
        assertTrue(
            ReminderTriggerEvaluator.isDateScheduleActive(
                date = null,
                repeatDays = emptyList(),
                monthlyDays = emptyList(),
                yearlyDates = listOf(vn.io.litever.remind.core.model.ReminderMonthDay(2, 29)),
                targetDate = leapFeb29
            )
        )
    }
}
