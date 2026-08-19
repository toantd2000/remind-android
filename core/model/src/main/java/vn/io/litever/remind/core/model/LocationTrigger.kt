package vn.io.litever.remind.core.model

import java.io.Serializable

data class LocationTrigger(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 100f,
    val locationName: String = "",
    val transitionType: GeofenceTransitionType = GeofenceTransitionType.ENTER,
    val timeWindow: ReminderTimeWindow? = null
) : Serializable
