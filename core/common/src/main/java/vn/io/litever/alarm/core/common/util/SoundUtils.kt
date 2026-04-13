package vn.io.litever.alarm.core.common.util

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

fun getAccessibleRingtoneUri(context: Context, uriString: String?): Uri {
    // 1. Try provided URI
    if (!uriString.isNullOrEmpty()) {
        try {
            val uri = Uri.parse(uriString)
            if (isUriAccessible(context, uri)) return uri
        } catch (e: Exception) {
            // Ignore and fallback
        }
    }
    
    // 2. Try Default Alarm
    val defaultAlarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    if (isUriAccessible(context, defaultAlarmUri)) return defaultAlarmUri
    
    // 3. Try Default Ringtone
    val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    if (isUriAccessible(context, defaultRingtoneUri)) return defaultRingtoneUri

    // 4. Try first available sound from RingtoneManager
    try {
        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_ALARM)
        val cursor = manager.cursor
        if (cursor != null && cursor.moveToFirst()) {
            return manager.getRingtoneUri(cursor.position)
        }
    } catch (e: Exception) {
        // Ignore and fallback
    }
    
    // 5. Final fallback
    return defaultAlarmUri ?: Uri.EMPTY
}

private fun isUriAccessible(context: Context, uri: Uri?): Boolean {
    if (uri == null) return false
    return try {
        val ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone != null
    } catch (e: Exception) {
        false
    }
}
