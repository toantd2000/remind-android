package vn.io.litever.remind.core.alarm.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.alarm.receiver.ReminderReceiver
import vn.io.litever.remind.core.alarm.reminder.ReminderTriggerEvaluator
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.Reminder
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationManager: LocationManager
) : LocationListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isTracking = false
    private var activeReminders: List<Reminder> = emptyList()

    // Key: reminderId, Value: true = inside, false = outside
    private val geofenceStateMap = ConcurrentHashMap<Long, Boolean>()

    fun startTracking(reminders: List<Reminder>) {
        this.activeReminders = reminders
        if (!isTracking) {
            registerLocationUpdates()
        }
        checkLastKnownLocation()
    }

    fun stopTracking() {
        try {
            locationManager.removeUpdates(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isTracking = false
        geofenceStateMap.clear()
    }

    private fun registerLocationUpdates() {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return

        try {
            val minTimeMs = 15_000L // 15 seconds
            val minDistanceMeters = 10f // 10 meters

            var registeredAny = false
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTimeMs,
                    minDistanceMeters,
                    this,
                    Looper.getMainLooper()
                )
                registeredAny = true
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTimeMs,
                    minDistanceMeters,
                    this,
                    Looper.getMainLooper()
                )
                registeredAny = true
            }
            isTracking = registeredAny
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkLastKnownLocation() {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return

        try {
            val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLoc = when {
                gpsLoc != null && netLoc != null -> if (gpsLoc.time >= netLoc.time) gpsLoc else netLoc
                gpsLoc != null -> gpsLoc
                else -> netLoc
            }
            bestLoc?.let { loc ->
                // Pre-populate initial geofence state without firing premature triggers on startup
                activeReminders.forEach { reminder ->
                    val locTrigger = reminder.locationTrigger
                    if (locTrigger != null) {
                        val distance = ReminderTriggerEvaluator.calculateHaversineDistanceMeters(
                            locTrigger.latitude, locTrigger.longitude,
                            loc.latitude, loc.longitude
                        )
                        geofenceStateMap[reminder.id] = distance <= locTrigger.radiusMeters
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        evaluateLocation(location.latitude, location.longitude)
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    fun evaluateLocation(lat: Double, lon: Double, now: LocalDateTime = LocalDateTime.now()) {
        activeReminders.forEach { reminder ->
            val result = ReminderTriggerEvaluator.evaluateLocationReminder(
                reminder = reminder,
                currentLatitude = lat,
                currentLongitude = lon,
                wasInside = geofenceStateMap[reminder.id],
                currentDateTime = now
            )

            result.isInsideGeofence?.let { isInside ->
                geofenceStateMap[reminder.id] = isInside
            }

            if (result.isTriggered) {
                dispatchTrigger(reminder)
            }
        }
    }

    private fun dispatchTrigger(reminder: Reminder) {
        scope.launch {
            val triggerIntent = Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_TRIGGER_REMINDER
                putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
                putExtra(ReminderReceiver.EXTRA_IS_SNOOZE, false)
            }
            context.sendBroadcast(triggerIntent)
        }
    }
}
