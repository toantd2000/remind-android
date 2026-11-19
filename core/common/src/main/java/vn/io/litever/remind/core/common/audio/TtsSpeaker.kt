package vn.io.litever.remind.core.common.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight Android TextToSpeech wrapper for speaking alarm labels, reminder titles,
 * and periodic time announcements using the appropriate audio stream.
 */
@Singleton
class TtsSpeaker internal constructor(
    private val context: Context,
    private val engineFactory: (Context, TextToSpeech.OnInitListener) -> TextToSpeech
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context = context,
        engineFactory = { ctx, listener -> TextToSpeech(ctx, listener) }
    )

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var pendingAlarmStream: Boolean = true
    private var pendingLocale: Locale = Locale.getDefault()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isLanguageDataMissing = MutableStateFlow(false)
    val isLanguageDataMissing: StateFlow<Boolean> = _isLanguageDataMissing.asStateFlow()

    fun speak(
        text: String,
        useAlarmStream: Boolean = true,
        locale: Locale? = null
    ) {
        if (text.isBlank()) return

        val targetLocale = locale ?: detectLocale(text)
        _isSpeaking.value = true

        if (tts == null) {
            pendingText = text
            pendingAlarmStream = useAlarmStream
            pendingLocale = targetLocale
            tts = engineFactory(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    tts?.setOnUtteranceProgressListener(utteranceListener)
                    pendingText?.let { queued ->
                        speakInternal(queued, pendingAlarmStream, pendingLocale)
                        pendingText = null
                    }
                } else {
                    Log.w(TAG, "TextToSpeech initialization failed with status=$status")
                    isInitialized = false
                    pendingText = null
                    _isSpeaking.value = false
                    runCatching { tts?.shutdown() }
                    tts = null
                }
            }
        } else if (isInitialized) {
            speakInternal(text, useAlarmStream, targetLocale)
        } else {
            pendingText = text
            pendingAlarmStream = useAlarmStream
            pendingLocale = targetLocale
        }
    }

    private fun speakInternal(text: String, useAlarmStream: Boolean, locale: Locale) {
        val engine = tts ?: return
        runCatching {
            val langResult = engine.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                _isLanguageDataMissing.value = true
                engine.setLanguage(Locale.ENGLISH)
            } else {
                _isLanguageDataMissing.value = false
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
        }.onFailure { error ->
            Log.e(TAG, "Failed to speak TTS text", error)
            _isSpeaking.value = false
        }
    }

    fun stop() {
        pendingText = null
        _isSpeaking.value = false
        runCatching {
            tts?.stop()
        }
    }

    private val utteranceListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
        }

        override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            _isSpeaking.value = false
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            _isSpeaking.value = false
        }
    }

    companion object {
        private const val TAG = "TtsSpeaker"
        private val VIETNAMESE_CHAR_REGEX = Regex(
            "[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ" +
                "ÀÁẢÃẠĂẰẮẲẴẶÂẦẤẨẪẬÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ]"
        )
        val LOCALE_VI: Locale = Locale.forLanguageTag("vi-VN")
        val LOCALE_EN: Locale = Locale.US
        val LOCALE_ZH: Locale = Locale.SIMPLIFIED_CHINESE
        val LOCALE_JA: Locale = Locale.JAPAN
        val LOCALE_KO: Locale = Locale.KOREA

        fun detectLocale(text: String, fallbackLocale: Locale = Locale.getDefault()): Locale {
            if (VIETNAMESE_CHAR_REGEX.containsMatchIn(text)) {
                return LOCALE_VI
            }
            if (text.any { it in '\u3040'..'\u309F' || it in '\u30A0'..'\u30FF' }) {
                return LOCALE_JA
            }
            if (text.any { it in '\uAC00'..'\uD7AF' || it in '\u1100'..'\u11FF' }) {
                return LOCALE_KO
            }
            if (text.any { it in '\u4E00'..'\u9FFF' || it in '\u3400'..'\u4DBF' }) {
                return LOCALE_ZH
            }
            val hasNonAsciiLetters = text.any { it.isLetter() && it.code > 127 }
            if (hasNonAsciiLetters && fallbackLocale.language != "en") {
                return fallbackLocale
            }
            return LOCALE_EN
        }
    }
}

enum class TtsLanguageOption(val tag: String, val locale: Locale?) {
    AUTO("AUTO", null),
    VIETNAMESE("vi-VN", TtsSpeaker.LOCALE_VI),
    ENGLISH("en-US", TtsSpeaker.LOCALE_EN),
    CHINESE("zh-CN", TtsSpeaker.LOCALE_ZH);

    fun resolveLocale(text: String, fallbackLocale: Locale = Locale.getDefault()): Locale =
        locale ?: TtsSpeaker.detectLocale(text, fallbackLocale)

    companion object {
        fun fromTag(tag: String?): TtsLanguageOption =
            entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: AUTO
    }
}
