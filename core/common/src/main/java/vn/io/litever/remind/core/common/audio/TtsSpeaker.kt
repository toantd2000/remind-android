package vn.io.litever.remind.core.common.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight Android TextToSpeech wrapper for speaking alarm labels, reminder titles,
 * and periodic time announcements using the appropriate audio stream.
 */
@Singleton
class TtsSpeaker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var pendingAlarmStream: Boolean = true
    private var pendingLocale: Locale = Locale.getDefault()

    fun speak(
        text: String,
        useAlarmStream: Boolean = true,
        locale: Locale = Locale.getDefault()
    ) {
        if (text.isBlank()) return

        if (tts == null) {
            pendingText = text
            pendingAlarmStream = useAlarmStream
            pendingLocale = locale
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    pendingText?.let { queued ->
                        speakInternal(queued, pendingAlarmStream, pendingLocale)
                        pendingText = null
                    }
                }
            }
        } else if (isInitialized) {
            speakInternal(text, useAlarmStream, locale)
        } else {
            pendingText = text
            pendingAlarmStream = useAlarmStream
            pendingLocale = locale
        }
    }

    private fun speakInternal(text: String, useAlarmStream: Boolean, locale: Locale) {
        val engine = tts ?: return
        runCatching {
            val langResult = engine.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.ENGLISH)
            }
            val usage = if (useAlarmStream) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA
            engine.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            val params = Bundle().apply {
                putInt(
                    TextToSpeech.Engine.KEY_PARAM_STREAM,
                    if (useAlarmStream) android.media.AudioManager.STREAM_ALARM
                    else android.media.AudioManager.STREAM_MUSIC
                )
            }
            engine.speak(text, TextToSpeech.QUEUE_ADD, params, "remind_tts_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        pendingText = null
        runCatching {
            tts?.stop()
        }
    }
}
