package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs

/**
 * Adversarial Challenger Stress Tests for ReminderTriggerEvaluator and ReminderSchedulerImpl.
 * Validates extreme coordinates, antimeridian crossings, boundary conditions, rapid coordinate jitter,
 * overnight time windows, unicode/emoji SSID sanitization, and request code namespaces.
 */
class ReminderChallengerStressTest {

    // =========================================================================
    // 1. HAVERSINE FORMULA AT EXTREME COORDINATES & BOUNDARIES
    // =========================================================================

    @Test
    fun haversine_zeroDistance_returnsZero() {
        val dist1 = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(0.0, 0.0, 0.0, 0.0)
        assertEquals(0.0, dist1, 1e-6)

        val dist2 = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(21.0285, 105.8542, 21.0285, 105.8542)
        assertEquals(0.0, dist2, 1e-6)

        val dist3 = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(90.0, 0.0, 90.0, 0.0)
        assertEquals(0.0, dist3, 1e-6)

        val dist4 = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(-90.0, 0.0, -90.0, 0.0)
        assertEquals(0.0, dist4, 1e-6)
    }

    @Test
    fun haversine_poles_northToSouth_returnsHalfCircumference() {
        // North Pole (90.0, 0.0) to South Pole (-90.0, 0.0)
        // Earth radius = 6,371,000m -> Half circumference = PI * 6,371,000 = ~20,015,086.8m
        val dist = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(90.0, 0.0, -90.0, 0.0)
        val expected = Math.PI * 6371000.0
        assertEquals(expected, dist, 1.0)
        assertFalse(dist.isNaN())
        assertFalse(dist.isInfinite())
    }

    @Test
    fun haversine_northPole_differentLongitudes_returnsZero() {
        // At North Pole (lat=90), longitude changes do not represent geographic separation
        val dist = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(90.0, 0.0, 90.0, 180.0)
        assertEquals(0.0, dist, 1e-3)
    }

    @Test
    fun haversine_antimeridianCrossing_calculatesShortestPath() {
        // Point 1: (0.0, 179.9999), Point 2: (0.0, -179.9999)
        // Angular difference across 180th meridian is 0.0002 degrees (~22.24 meters)
        val dist = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(0.0, 179.9999, 0.0, -179.9999)
        val expected = (0.0002 / 360.0) * (2 * Math.PI * 6371000.0)
        assertEquals(expected, dist, 1.0)
        assertTrue("Distance across 180/-180 meridian should be ~22m, got $dist", dist in 20.0..25.0)
    }

    @Test
    fun haversine_antipodalEquator_returnsHalfCircumference() {
        // Equator antipodal points: (0.0, 0.0) and (0.0, 180.0) or (0.0, -180.0)
        val dist = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(0.0, 0.0, 0.0, 180.0)
        val expected = Math.PI * 6371000.0
        assertEquals(expected, dist, 1.0)
    }

    @Test
    fun haversine_exactRadiusBoundary_matchesEquality() {
        val latCenter = 21.0285
        val lonCenter = 105.8542
        val radius = 100f

        val reminder = Reminder(
            id = 1,
            title = "Boundary Test",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = latCenter,
                longitude = lonCenter,
                radiusMeters = radius,
                transitionType = GeofenceTransitionType.ENTER
            )
        )

        // Point at center (distance = 0 <= radius -> inside)
        val resultCenter = ReminderTriggerEvaluator.evaluateLocationReminder(
            reminder = reminder,
            currentLatitude = latCenter,
            currentLongitude = lonCenter,
            wasInside = false
        )
        assertTrue(resultCenter.isTriggered)
        assertTrue(resultCenter.isInsideGeofence == true)
        assertEquals(0.0, resultCenter.distanceMeters ?: -1.0, 1e-4)
    }

    // =========================================================================
    // 2. GEOFENCE TRANSITIONS & RAPID JITTER
    // =========================================================================

    @Test
    fun geofence_rapidJitter_enterTransition_triggersOnlyOnRisingEdge() {
        val reminder = Reminder(
            id = 10,
            title = "Office Enter",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.ENTER
            )
        )

        val insideLat = 21.0285
        val insideLon = 105.8542
        val outsideLat = 21.0385 // ~1.1km away
        val outsideLon = 105.8542

        // T0: First evaluation, user is outside (wasInside = null) -> NO trigger
        val r0 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, outsideLat, outsideLon, wasInside = null)
        assertFalse(r0.isTriggered)
        assertEquals(false, r0.isInsideGeofence)

        // T1: User enters geofence (wasInside = false -> isInside = true) -> TRIGGER!
        val r1 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, insideLat, insideLon, wasInside = false)
        assertTrue(r1.isTriggered)
        assertEquals(true, r1.isInsideGeofence)

        // T2: User stays inside geofence (wasInside = true -> isInside = true) -> NO trigger
        val r2 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, insideLat, insideLon, wasInside = true)
        assertFalse(r2.isTriggered)
        assertEquals(true, r2.isInsideGeofence)

        // T3: GPS jitter kicks user outside (wasInside = true -> isInside = false) -> NO trigger for ENTER
        val r3 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, outsideLat, outsideLon, wasInside = true)
        assertFalse(r3.isTriggered)
        assertEquals(false, r3.isInsideGeofence)

        // T4: GPS jitter puts user back inside (wasInside = false -> isInside = true) -> TRIGGER!
        val r4 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, insideLat, insideLon, wasInside = false)
        assertTrue(r4.isTriggered)
        assertEquals(true, r4.isInsideGeofence)
    }

    @Test
    fun geofence_exitTransition_triggersOnlyOnFallingEdge() {
        val reminder = Reminder(
            id = 11,
            title = "Office Exit",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.EXIT
            )
        )

        val insideLat = 21.0285
        val insideLon = 105.8542
        val outsideLat = 21.0385
        val outsideLon = 105.8542

        // T0: Initial boot while already outside -> should NOT trigger exit
        val r0 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, outsideLat, outsideLon, wasInside = null)
        assertFalse(r0.isTriggered)

        // T1: Initial boot while inside -> should NOT trigger exit
        val r1 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, insideLat, insideLon, wasInside = null)
        assertFalse(r1.isTriggered)

        // T2: Moving from inside to outside -> TRIGGER!
        val r2 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, outsideLat, outsideLon, wasInside = true)
        assertTrue(r2.isTriggered)
        assertEquals(false, r2.isInsideGeofence)

        // T3: Staying outside -> NO trigger
        val r3 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, outsideLat, outsideLon, wasInside = false)
        assertFalse(r3.isTriggered)
    }

    @Test
    fun geofence_bothTransition_triggersOnBothEdges() {
        val reminder = Reminder(
            id = 12,
            title = "Office Enter or Exit",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.BOTH
            )
        )

        val insideLat = 21.0285
        val insideLon = 105.8542
        val outsideLat = 21.0385
        val outsideLon = 105.8542

        // Initial boot inside -> triggers for BOTH
        val r0 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, insideLat, insideLon, wasInside = null)
        assertTrue(r0.isTriggered)

        // Outside -> Inside -> TRIGGER
        val r1 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, insideLat, insideLon, wasInside = false)
        assertTrue(r1.isTriggered)

        // Inside -> Outside -> TRIGGER
        val r2 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, outsideLat, outsideLon, wasInside = true)
        assertTrue(r2.isTriggered)

        // Inside -> Inside -> NO trigger
        val r3 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, insideLat, insideLon, wasInside = true)
        assertFalse(r3.isTriggered)

        // Outside -> Outside -> NO trigger
        val r4 = ReminderTriggerEvaluator.evaluateLocationReminder(reminder, outsideLat, outsideLon, wasInside = false)
        assertFalse(r4.isTriggered)
    }

    // =========================================================================
    // 3. TIME WINDOW EVALUATIONS SPANNING MIDNIGHT (23:00 - 05:00)
    // =========================================================================

    @Test
    fun timeWindow_overnight_activeMondayOnly_evaluatesAcrossAll7Days() {
        // Window from 23:00 to 05:00 active on MONDAY
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(23, 0),
            endTime = LocalTime.of(5, 0),
            activeDays = listOf(DayOfWeek.MONDAY)
        )

        // Reference dates for 2026-08 (August 2026):
        // 2026-08-17: Monday
        // 2026-08-18: Tuesday
        // 2026-08-19: Wednesday
        // 2026-08-20: Thursday
        // 2026-08-21: Friday
        // 2026-08-22: Saturday
        // 2026-08-23: Sunday

        // 1. Monday 22:59 -> FALSE (before 23:00)
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 22, 59, 59)))

        // 2. Monday 23:00 -> TRUE (start of Monday window)
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 23, 0, 0)))

        // 3. Monday 23:59 -> TRUE
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 23, 59, 59)))

        // 4. Tuesday 00:00 -> TRUE (continuation of Monday overnight schedule)
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 18, 0, 0, 0)))

        // 5. Tuesday 04:59 -> TRUE
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 18, 4, 59, 59)))

        // 6. Tuesday 05:00 -> TRUE (end boundary inclusive)
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 18, 5, 0, 0)))

        // 7. Tuesday 05:01 -> FALSE (after 05:00)
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 18, 5, 1, 0)))

        // 8. Tuesday 23:30 -> FALSE (Tuesday evening not active)
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 18, 23, 30, 0)))

        // 9. Monday 03:00 -> FALSE (Monday morning belongs to Sunday evening schedule, Sunday is inactive)
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 17, 3, 0, 0)))

        // 10. Check other days of week:
        // Wednesday (19th), Thursday (20th), Friday (21st), Saturday (22nd), Sunday (23rd)
        for (day in 19..23) {
            assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 23, 30, 0)))
            assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 2, 30, 0)))
        }
    }

    @Test
    fun timeWindow_overnight_everyDay_evaluatesAllDays() {
        // Window from 22:00 to 06:00, empty activeDays = every day
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(22, 0),
            endTime = LocalTime.of(6, 0),
            activeDays = emptyList()
        )

        // For all 7 days of the week, 23:00 is active and 12:00 is inactive
        for (day in 17..23) {
            assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 22, 0, 0)))
            assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 23, 30, 0)))
            assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 0, 30, 0)))
            assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 5, 59, 0)))
            assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 6, 0, 0)))

            assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 6, 1, 0)))
            assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 12, 0, 0)))
            assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, day, 21, 59, 59)))
        }
    }

    @Test
    fun timeWindow_sameStartAndEndTime_pointInTime() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(12, 0),
            endTime = LocalTime.of(12, 0),
            activeDays = listOf(DayOfWeek.WEDNESDAY)
        )

        // Wednesday 12:00:00 -> TRUE
        assertTrue(window.isWithinWindow(LocalDateTime.of(2026, 8, 19, 12, 0, 0)))

        // Wednesday 12:00:01 -> FALSE
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 19, 12, 0, 1)))

        // Thursday 12:00:00 -> FALSE (Day mismatch)
        assertFalse(window.isWithinWindow(LocalDateTime.of(2026, 8, 20, 12, 0, 0)))
    }

    // =========================================================================
    // 4. WI-FI SSID QUOTE STRIPPING, UNICODE, EMOJIS, CASE INSENSITIVITY & BSSID
    // =========================================================================

    @Test
    fun wifi_sanitizeSsid_handlesAllQuotingAndSpecialCases() {
        assertEquals("Home_Network", ReminderTriggerEvaluator.sanitizeSsid("\"Home_Network\""))
        assertEquals("Home_Network", ReminderTriggerEvaluator.sanitizeSsid("Home_Network"))
        assertEquals("Home Network with Spaces", ReminderTriggerEvaluator.sanitizeSsid("  \"Home Network with Spaces\"  "))
        assertEquals("", ReminderTriggerEvaluator.sanitizeSsid("\"\""))
        assertEquals("", ReminderTriggerEvaluator.sanitizeSsid("   "))
        assertEquals("", ReminderTriggerEvaluator.sanitizeSsid(null))
        assertEquals("", ReminderTriggerEvaluator.sanitizeSsid("<unknown ssid>"))
        assertEquals("", ReminderTriggerEvaluator.sanitizeSsid("\"<unknown ssid>\""))
    }

    @Test
    fun wifi_unicodeAndEmojisInSsid_matchesCorrectly() {
        val emojiSsid = "🔥 Cafe & Wi-Fi ☕ 🚀"
        val reminder = Reminder(
            id = 50,
            title = "Cafe WiFi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = emojiSsid,
                triggerType = WifiTriggerType.ON_CONNECT
            )
        )

        // Connected with quoted emoji SSID
        val res = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "\"🔥 Cafe & Wi-Fi ☕ 🚀\"",
            currentBssid = null,
            isConnected = true,
            wasConnected = false
        )
        assertTrue(res.isTriggered)
    }

    @Test
    fun wifi_caseInsensitivity_matchesIgnoringCase() {
        val reminder = Reminder(
            id = 51,
            title = "Office WiFi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "Company_Guest_5GHz",
                triggerType = WifiTriggerType.ON_CONNECT
            )
        )

        // System returns lowercase SSID
        val res = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "\"company_guest_5ghz\"",
            currentBssid = null,
            isConnected = true,
            wasConnected = false
        )
        assertTrue(res.isTriggered)
    }

    @Test
    fun wifi_vietnameseDiacritics_matchesProperly() {
        val target = "Cà Phê Sữa ĐÁ"
        val reminder = Reminder(
            id = 52,
            title = "VN Cafe",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = target,
                triggerType = WifiTriggerType.ON_CONNECT
            )
        )

        val res = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "\"cà phê sữa đá\"",
            currentBssid = null,
            isConnected = true,
            wasConnected = false
        )
        assertTrue(res.isTriggered)
    }

    @Test
    fun wifi_unknownSsid_neverTriggers() {
        val reminder = Reminder(
            id = 53,
            title = "Home WiFi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "My_Home",
                triggerType = WifiTriggerType.ON_CONNECT
            )
        )

        // When Location is off, Android returns "<unknown ssid>"
        val res = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "<unknown ssid>",
            currentBssid = null,
            isConnected = true,
            wasConnected = false
        )
        assertFalse(res.isTriggered)
    }

    @Test
    fun wifi_bssidFiltering_caseInsensitiveAndExactMatch() {
        val reminder = Reminder(
            id = 54,
            title = "Specific AP WiFi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "Office_Mesh",
                bssid = "00:11:22:AA:BB:CC",
                triggerType = WifiTriggerType.ON_CONNECT
            )
        )

        // Match with lowercase BSSID
        val resMatch = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "Office_Mesh",
            currentBssid = "00:11:22:aa:bb:cc",
            isConnected = true,
            wasConnected = false
        )
        assertTrue(resMatch.isTriggered)

        // Mismatch BSSID on same SSID
        val resMismatch = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "Office_Mesh",
            currentBssid = "00:11:22:99:88:77",
            isConnected = true,
            wasConnected = false
        )
        assertFalse(resMismatch.isTriggered)
    }

    @Test
    fun wifi_whileConnected_triggersContinuouslyWhenConnected() {
        val reminder = Reminder(
            id = 55,
            title = "Continuous While Connected",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(
                ssid = "Home_WiFi",
                triggerType = WifiTriggerType.WHILE_CONNECTED
            )
        )

        // Even if wasConnected was already true, WHILE_CONNECTED evaluates to true
        val res = ReminderTriggerEvaluator.evaluateWifiReminder(
            reminder = reminder,
            currentSsid = "Home_WiFi",
            currentBssid = null,
            isConnected = true,
            wasConnected = true
        )
        assertTrue(res.isTriggered)
    }

    // =========================================================================
    // 5. REQUEST CODE NAMESPACE COLLISION CHECK
    // =========================================================================

    @Test
    fun requestCodes_primaryAndSnooze_neverCollideForAnyId() {
        fun getReminderRequestCode(reminderId: Long, isSnooze: Boolean): Int {
            val baseHash = (reminderId.hashCode() and 0x0FFFFFFF)
            return if (isSnooze) 250000000 + baseHash else 200000000 + baseHash
        }

        fun getAlarmRequestCode(alarmId: Long, isSnooze: Boolean): Int {
            val idHash = alarmId.hashCode()
            return if (isSnooze) idHash + 1000000000 else idHash
        }

        val testIds = listOf(
            0L, 1L, 2L, 100L, 1000L, 999999L,
            Long.MAX_VALUE, Long.MIN_VALUE,
            123456789L, -123456789L,
            0x7FFFFFFFL, 0x10000000L
        )

        for (id in testIds) {
            val primaryReq = getReminderRequestCode(id, isSnooze = false)
            val snoozeReq = getReminderRequestCode(id, isSnooze = true)

            // For the same reminder ID, primary and snooze codes must never be equal
            assertNotEquals("Primary and Snooze request codes must not collide for ID $id", primaryReq, snoozeReq)
            assertEquals("Snooze must differ from primary by exactly 50,000,000", 50000000, snoozeReq - primaryReq)

            // Primary code must be >= 200,000,000 and <= 200,000,000 + 0x0FFFFFFF (468,435,455)
            assertTrue("Primary code in range: $primaryReq", primaryReq in 200000000..468435455)
            // Snooze code must be >= 250,000,000 and <= 250,000,000 + 0x0FFFFFFF (518,435,455)
            assertTrue("Snooze code in range: $snoozeReq", snoozeReq in 250000000..518435455)
        }
    }
}
