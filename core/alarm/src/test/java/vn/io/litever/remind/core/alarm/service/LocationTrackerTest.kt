package vn.io.litever.remind.core.alarm.service

import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.receiver.ReminderReceiver
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType

@RunWith(RobolectricTestRunner::class)
class LocationTrackerTest {

    private val context = mockk<Context>(relaxed = true)
    private val locationManager = mockk<LocationManager>(relaxed = true)

    private lateinit var locationTracker: LocationTracker

    @Before
    fun setup() {
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns true
        every { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } returns true

        locationTracker = LocationTracker(context, locationManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun startTracking_registersUpdates_whenPermissionGranted() {
        val reminder = Reminder(
            id = 1L,
            title = "Office",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f)
        )

        locationTracker.startTracking(listOf(reminder))

        verify(atLeast = 1) {
            locationManager.requestLocationUpdates(any<String>(), any<Long>(), any<Float>(), any<android.location.LocationListener>(), any())
        }
    }

    @Test
    fun stopTracking_removesUpdates() {
        locationTracker.stopTracking()

        verify(atLeast = 1) {
            locationManager.removeUpdates(locationTracker)
        }
    }

    @Test
    fun evaluateLocation_triggersWhenInsideGeofence() {
        val reminder = Reminder(
            id = 10L,
            title = "Store Arrival",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 100f,
                transitionType = GeofenceTransitionType.ENTER
            )
        )

        locationTracker.startTracking(listOf(reminder))

        // First evaluate location inside geofence (20 meters away)
        locationTracker.evaluateLocation(21.0286, 105.8542)
        Thread.sleep(100)

        verify(atLeast = 1) {
            context.sendBroadcast(match {
                it.action == ReminderReceiver.ACTION_TRIGGER_REMINDER &&
                        it.getLongExtra(ReminderReceiver.EXTRA_REMINDER_ID, -1L) == 10L
            })
        }
    }

    @Test
    fun evaluateLocation_doesNotTriggerWhenOutsideGeofence() {
        val reminder = Reminder(
            id = 20L,
            title = "Far Away",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 21.0285,
                longitude = 105.8542,
                radiusMeters = 50f,
                transitionType = GeofenceTransitionType.ENTER
            )
        )

        locationTracker.startTracking(listOf(reminder))

        // Evaluate location 500 meters away
        locationTracker.evaluateLocation(21.0244, 105.8573)
        Thread.sleep(100)

        verify(exactly = 0) {
            context.sendBroadcast(match {
                it.action == ReminderReceiver.ACTION_TRIGGER_REMINDER &&
                        it.getLongExtra(ReminderReceiver.EXTRA_REMINDER_ID, -1L) == 20L
            })
        }
    }
}
