package vn.io.litever.remind.core.alarm.service

import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.alarm.receiver.ReminderReceiver
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType

@RunWith(RobolectricTestRunner::class)
class WifiTrackerTest {

    private val context = mockk<Context>(relaxed = true)
    private val connectivityManager = mockk<ConnectivityManager>(relaxed = true)
    private val wifiManager = mockk<WifiManager>(relaxed = true)

    private lateinit var wifiTracker: WifiTracker

    @Before
    fun setup() {
        wifiTracker = WifiTracker(context, connectivityManager, wifiManager)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun startTracking_registersNetworkCallback() {
        val reminder = Reminder(
            id = 1L,
            title = "Home Wifi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Home_5G")
        )

        wifiTracker.startTracking(listOf(reminder))

        verify(atLeast = 1) {
            connectivityManager.registerNetworkCallback(any(), any<ConnectivityManager.NetworkCallback>())
        }
    }

    @Test
    fun stopTracking_unregistersNetworkCallback() {
        val reminder = Reminder(
            id = 1L,
            title = "Home Wifi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Home_5G")
        )

        wifiTracker.startTracking(listOf(reminder))
        wifiTracker.stopTracking()

        verify(atLeast = 1) {
            connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())
        }
    }

    @Test
    fun evaluateWifiEvent_onConnectMatch_triggersBroadcast() {
        val reminder = Reminder(
            id = 10L,
            title = "Work Wifi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Office_WiFi", triggerType = WifiTriggerType.ON_CONNECT)
        )

        wifiTracker.startTracking(listOf(reminder))

        wifiTracker.evaluateWifiEvent(
            ssid = "\"Office_WiFi\"",
            bssid = null,
            isConnected = true,
            event = WifiTriggerType.ON_CONNECT
        )
        Thread.sleep(100)

        verify(atLeast = 1) {
            context.sendBroadcast(match {
                it.action == ReminderReceiver.ACTION_TRIGGER_REMINDER &&
                        it.getLongExtra(ReminderReceiver.EXTRA_REMINDER_ID, -1L) == 10L
            })
        }
    }

    @Test
    fun evaluateWifiEvent_onConnectMismatch_doesNotTriggerBroadcast() {
        val reminder = Reminder(
            id = 20L,
            title = "Work Wifi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Office_WiFi", triggerType = WifiTriggerType.ON_CONNECT)
        )

        wifiTracker.startTracking(listOf(reminder))

        wifiTracker.evaluateWifiEvent(
            ssid = "\"Other_WiFi\"",
            bssid = null,
            isConnected = true,
            event = WifiTriggerType.ON_CONNECT
        )
        Thread.sleep(100)

        verify(exactly = 0) {
            context.sendBroadcast(match {
                it.action == ReminderReceiver.ACTION_TRIGGER_REMINDER &&
                        it.getLongExtra(ReminderReceiver.EXTRA_REMINDER_ID, -1L) == 20L
            })
        }
    }

    @Test
    fun evaluateWifiEvent_onDisconnectMatch_triggersBroadcast() {
        val reminder = Reminder(
            id = 30L,
            title = "Leave Home Wifi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Home_Network", triggerType = WifiTriggerType.ON_DISCONNECT)
        )

        wifiTracker.startTracking(listOf(reminder))

        // Initial connect
        wifiTracker.evaluateWifiEvent(
            ssid = "Home_Network",
            bssid = null,
            isConnected = true,
            event = WifiTriggerType.ON_CONNECT
        )

        // Then disconnect
        wifiTracker.evaluateWifiEvent(
            ssid = "Home_Network",
            bssid = null,
            isConnected = false,
            event = WifiTriggerType.ON_DISCONNECT
        )
        Thread.sleep(100)

        verify(atLeast = 1) {
            context.sendBroadcast(match {
                it.action == ReminderReceiver.ACTION_TRIGGER_REMINDER &&
                        it.getLongExtra(ReminderReceiver.EXTRA_REMINDER_ID, -1L) == 30L
            })
        }
    }
}
