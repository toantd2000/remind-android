package vn.io.litever.remind.core.alarm.service

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.alarm.receiver.ReminderReceiver
import vn.io.litever.remind.core.alarm.reminder.ReminderTriggerEvaluator
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectivityManager: ConnectivityManager,
    private val wifiManager: WifiManager
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isTracking = false
    private var activeReminders: List<Reminder> = emptyList()

    private var lastConnectedSsid: String? = null
    private var lastConnectedBssid: String? = null

    // Key: reminderId, Value: true = connected to target, false = not connected
    private val wifiConnectedStateMap = ConcurrentHashMap<Long, Boolean>()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    capabilities.transportInfo as? WifiInfo
                } else {
                    @Suppress("DEPRECATION")
                    wifiManager.connectionInfo
                }

                @Suppress("DEPRECATION")
                val rawSsid = wifiInfo?.ssid ?: wifiManager.connectionInfo?.ssid
                @Suppress("DEPRECATION")
                val bssid = wifiInfo?.bssid ?: wifiManager.connectionInfo?.bssid
                val ssid = ReminderTriggerEvaluator.sanitizeSsid(rawSsid)

                if (ssid.isNotBlank() && ssid != "<unknown ssid>") {
                    val isNewConnection = lastConnectedSsid != ssid || lastConnectedBssid != bssid
                    if (isNewConnection) {
                        lastConnectedSsid = ssid
                        lastConnectedBssid = bssid

                        evaluateWifiEvent(ssid, bssid, isConnected = true, event = WifiTriggerType.ON_CONNECT)
                        evaluateWifiEvent(ssid, bssid, isConnected = true, event = WifiTriggerType.WHILE_CONNECTED)
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            val previousSsid = lastConnectedSsid
            val previousBssid = lastConnectedBssid
            lastConnectedSsid = null
            lastConnectedBssid = null

            if (previousSsid != null) {
                evaluateWifiEvent(previousSsid, previousBssid, isConnected = false, event = WifiTriggerType.ON_DISCONNECT)
            }
        }
    }

    fun startTracking(reminders: List<Reminder>) {
        this.activeReminders = reminders
        if (!isTracking) {
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            try {
                connectivityManager.registerNetworkCallback(request, networkCallback)
                isTracking = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        checkCurrentWifiState()
    }

    fun stopTracking() {
        if (isTracking) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isTracking = false
            lastConnectedSsid = null
            lastConnectedBssid = null
            wifiConnectedStateMap.clear()
        }
    }

    private fun checkCurrentWifiState() {
        try {
            @Suppress("DEPRECATION")
            val info = wifiManager.connectionInfo
            val ssid = ReminderTriggerEvaluator.sanitizeSsid(info?.ssid)
            if (ssid.isNotBlank() && ssid != "<unknown ssid>") {
                lastConnectedSsid = ssid
                lastConnectedBssid = info?.bssid
                activeReminders.forEach { reminder ->
                    val wifiTrigger = reminder.wifiTrigger
                    if (wifiTrigger != null) {
                        val targetSsid = ReminderTriggerEvaluator.sanitizeSsid(wifiTrigger.ssid)
                        val isSsidMatch = targetSsid.isNotEmpty() && targetSsid.equals(ssid, ignoreCase = true)
                        val isBssidMatch = wifiTrigger.bssid.isNullOrBlank() || wifiTrigger.bssid.equals(info?.bssid, ignoreCase = true)
                        val isTargetNetwork = isSsidMatch && isBssidMatch
                        wifiConnectedStateMap[reminder.id] = isTargetNetwork

                        if (wifiTrigger.triggerType == WifiTriggerType.WHILE_CONNECTED && isTargetNetwork) {
                            val result = ReminderTriggerEvaluator.evaluateWifiReminder(
                                reminder = reminder,
                                currentSsid = ssid,
                                currentBssid = info?.bssid,
                                isConnected = true,
                                wasConnected = false
                            )
                            if (result.isTriggered) {
                                dispatchTrigger(reminder)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun evaluateWifiEvent(
        ssid: String?,
        bssid: String?,
        isConnected: Boolean,
        event: WifiTriggerType,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        activeReminders.forEach { reminder ->
            val result = ReminderTriggerEvaluator.evaluateWifiReminder(
                reminder = reminder,
                currentSsid = ssid,
                currentBssid = bssid,
                isConnected = isConnected,
                wasConnected = wifiConnectedStateMap[reminder.id],
                currentDateTime = now
            )

            result.isConnectedWifi?.let { connected ->
                wifiConnectedStateMap[reminder.id] = connected
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
