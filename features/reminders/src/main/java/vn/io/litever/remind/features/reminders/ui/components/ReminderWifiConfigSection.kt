package vn.io.litever.remind.features.reminders.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderWifiConfigSection(
    ssid: String,
    onSsidChange: (String) -> Unit,
    bssid: String?,
    onBssidChange: (String?) -> Unit,
    triggerType: WifiTriggerType,
    onTriggerTypeChange: (WifiTriggerType) -> Unit,
    timeWindow: ReminderTimeWindow?,
    onTimeWindowChange: (ReminderTimeWindow?) -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showTimeWindowDialog by remember { mutableStateOf(false) }

    LiteverOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_wifi_trigger),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // SSID input
            LiteverTextField(
                value = ssid,
                onValueChange = onSsidChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(R.string.wifi_ssid_placeholder),
                label = stringResource(R.string.wifi_ssid_label),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pick Connected Wi-Fi Button
            val noWifiMsg = stringResource(R.string.no_connected_wifi_msg)
            LiteverOutlinedButton(
                onClick = {
                    val (connectedSsid, connectedBssid) = getConnectedWifiInfo(context)
                    if (!connectedSsid.isNullOrBlank()) {
                        onSsidChange(connectedSsid)
                        if (!connectedBssid.isNullOrBlank() && bssid.isNullOrBlank()) {
                            onBssidChange(connectedBssid)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            noWifiMsg,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.WifiFind,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.use_connected_wifi),
                    style = LiteverTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BSSID input (optional)
            LiteverTextField(
                value = bssid ?: "",
                onValueChange = { onBssidChange(it.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(R.string.wifi_bssid_placeholder),
                label = stringResource(R.string.wifi_bssid_label),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wi-Fi Trigger Event Type
            Text(
                text = stringResource(R.string.wifi_trigger_type_label),
                style = LiteverTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                WifiTriggerType.entries.forEachIndexed { index, type ->
                    val isSelected = type == triggerType
                    val labelRes = when (type) {
                        WifiTriggerType.ON_CONNECT -> R.string.wifi_on_connect
                        WifiTriggerType.ON_DISCONNECT -> R.string.wifi_on_disconnect
                        WifiTriggerType.WHILE_CONNECTED -> R.string.wifi_while_connected
                    }

                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = WifiTriggerType.entries.size),
                        onClick = { onTriggerTypeChange(type) },
                        selected = isSelected,
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                style = LiteverTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Defined Time Window Trigger
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { showTimeWindowDialog = true },
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = null,
                            tint = LiteverTheme.colors.secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.time_window_section_title),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )
                            val summary = if (timeWindow != null) {
                                val s = TimeFormatUtils.formatTime(timeWindow.startTime, is24HourFormat)
                                val e = TimeFormatUtils.formatTime(timeWindow.endTime, is24HourFormat)
                                "$s - $e"
                            } else {
                                stringResource(R.string.time_window_desc)
                            }
                            Text(
                                text = summary,
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTimeWindowDialog) {
        ReminderTimeWindowDialog(
            initialTimeWindow = timeWindow,
            is24HourFormat = is24HourFormat,
            onDismissRequest = { showTimeWindowDialog = false },
            onConfirm = { newWindow ->
                onTimeWindowChange(newWindow)
                showTimeWindowDialog = false
            }
        )
    }
}

private fun getConnectedWifiInfo(context: Context): Pair<String?, String?> {
    return try {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        var foundSsid: String? = null
        var foundBssid: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(network)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                val wifiInfo = capabilities.transportInfo as? WifiInfo
                foundSsid = wifiInfo?.ssid
                foundBssid = wifiInfo?.bssid
            }
        }

        if (foundSsid.isNullOrBlank() || foundSsid == "<unknown ssid>") {
            @Suppress("DEPRECATION")
            val info = wifiManager?.connectionInfo
            foundSsid = info?.ssid
            foundBssid = info?.bssid
        }

        val clean = sanitizeSsid(foundSsid)
        val validSsid = if (clean.isNotBlank() && clean != "<unknown ssid>") clean else null
        Pair(validSsid, foundBssid)
    } catch (_: Exception) {
        Pair(null, null)
    }
}

private fun sanitizeSsid(raw: String?): String {
    if (raw == null) return ""
    return if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length >= 2) {
        raw.substring(1, raw.length - 1)
    } else {
        raw
    }
}

