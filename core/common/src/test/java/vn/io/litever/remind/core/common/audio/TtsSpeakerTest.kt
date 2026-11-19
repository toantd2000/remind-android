package vn.io.litever.remind.core.common.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class TtsSpeakerTest {

    private val context = mockk<Context>(relaxed = true)
    private val mockEngine = mockk<TextToSpeech>(relaxed = true)
    private var capturedListener: TextToSpeech.OnInitListener? = null
    private var engineCreateCount = 0

    private lateinit var ttsSpeaker: TtsSpeaker

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        mockkConstructor(AudioAttributes.Builder::class)
        every { anyConstructed<AudioAttributes.Builder>().setUsage(any()) } answers {
            self as AudioAttributes.Builder
        }
        every { anyConstructed<AudioAttributes.Builder>().setContentType(any()) } answers {
            self as AudioAttributes.Builder
        }
        every { anyConstructed<AudioAttributes.Builder>().build() } returns mockk(relaxed = true)

        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit

        every { mockEngine.setLanguage(any()) } returns TextToSpeech.LANG_AVAILABLE

        engineCreateCount = 0
        capturedListener = null
        ttsSpeaker = TtsSpeaker(context) { _, listener ->
            engineCreateCount++
            capturedListener = listener
            mockEngine
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun speak_whenInitSucceeds_speaksQueuedTextAndTracksSpeakingState() {
        val listenerSlot = slot<UtteranceProgressListener>()
        every { mockEngine.setOnUtteranceProgressListener(capture(listenerSlot)) } returns TextToSpeech.SUCCESS

        assertFalse(ttsSpeaker.isSpeaking.value)
        ttsSpeaker.speak("Wake up", useAlarmStream = false, locale = Locale.US)
        assertTrue(ttsSpeaker.isSpeaking.value)

        capturedListener?.onInit(TextToSpeech.SUCCESS)

        verify(exactly = 1) {
            mockEngine.speak("Wake up", TextToSpeech.QUEUE_ADD, any(), any())
        }

        listenerSlot.captured.onDone("id")
        assertFalse(ttsSpeaker.isSpeaking.value)
    }

    @Test
    fun speak_whenInitFails_resetsEngineAndRetriesOnNextSpeak() {
        ttsSpeaker.speak("First try", useAlarmStream = false, locale = Locale.US)
        assertEquals(1, engineCreateCount)
        assertTrue(ttsSpeaker.isSpeaking.value)
        capturedListener?.onInit(TextToSpeech.ERROR)

        assertFalse(ttsSpeaker.isSpeaking.value)
        verify(exactly = 1) { mockEngine.shutdown() }
        verify(exactly = 0) {
            mockEngine.speak(any<CharSequence>(), any(), any(), any())
        }

        ttsSpeaker.speak("Second try", useAlarmStream = false, locale = Locale.US)
        assertEquals(2, engineCreateCount)
        capturedListener?.onInit(TextToSpeech.SUCCESS)

        verify(exactly = 1) {
            mockEngine.speak("Second try", TextToSpeech.QUEUE_ADD, any(), any())
        }
    }

    @Test
    fun speak_whenAlreadyInitialized_speaksImmediately() {
        ttsSpeaker.speak("Hello", useAlarmStream = true, locale = Locale.US)
        capturedListener?.onInit(TextToSpeech.SUCCESS)

        ttsSpeaker.speak("World", useAlarmStream = true, locale = Locale.US)

        verify(exactly = 1) {
            mockEngine.speak("Hello", TextToSpeech.QUEUE_ADD, any(), any())
        }
        verify(exactly = 1) {
            mockEngine.speak("World", TextToSpeech.QUEUE_ADD, any(), any())
        }
    }

    @Test
    fun stop_clearsPendingTextAndStopsEngine() {
        ttsSpeaker.speak("Pending", useAlarmStream = false, locale = Locale.US)
        assertTrue(ttsSpeaker.isSpeaking.value)
        ttsSpeaker.stop()

        assertFalse(ttsSpeaker.isSpeaking.value)
        capturedListener?.onInit(TextToSpeech.SUCCESS)

        verify(exactly = 0) {
            mockEngine.speak(any<CharSequence>(), any(), any(), any())
        }
        verify(exactly = 1) { mockEngine.stop() }
    }

    @Test
    fun speak_whenLanguageMissing_setsIsLanguageDataMissingAndFallsBackToEnglish() {
        every { mockEngine.setLanguage(Locale.forLanguageTag("vi-VN")) } returns TextToSpeech.LANG_MISSING_DATA
        every { mockEngine.setLanguage(Locale.ENGLISH) } returns TextToSpeech.LANG_AVAILABLE

        ttsSpeaker.speak("Xin chào", useAlarmStream = false, locale = Locale.forLanguageTag("vi-VN"))
        capturedListener?.onInit(TextToSpeech.SUCCESS)

        assertTrue(ttsSpeaker.isLanguageDataMissing.value)
        verify(exactly = 1) { mockEngine.setLanguage(Locale.ENGLISH) }
    }

    @Test
    fun speak_whenLanguageAvailable_clearsIsLanguageDataMissing() {
        every { mockEngine.setLanguage(Locale.US) } returns TextToSpeech.LANG_AVAILABLE

        ttsSpeaker.speak("Hello", useAlarmStream = false, locale = Locale.US)
        capturedListener?.onInit(TextToSpeech.SUCCESS)

        assertFalse(ttsSpeaker.isLanguageDataMissing.value)
    }

    @Test
    fun detectLocale_detectsVietnameseEnglishAndCjkCorrectly() {
        assertEquals(TtsSpeaker.LOCALE_VI, TtsSpeaker.detectLocale("Thức dậy tập thể dục", Locale.US))
        assertEquals(TtsSpeaker.LOCALE_EN, TtsSpeaker.detectLocale("Morning Workout", TtsSpeaker.LOCALE_VI))
        assertEquals(TtsSpeaker.LOCALE_ZH, TtsSpeaker.detectLocale("早上好，起床了", Locale.US))
        assertEquals(TtsSpeaker.LOCALE_JA, TtsSpeaker.detectLocale("おはようございます", Locale.US))
        assertEquals(TtsSpeaker.LOCALE_KO, TtsSpeaker.detectLocale("좋은 아침입니다", Locale.US))
    }

    @Test
    fun speak_withNullLocale_autoDetectsVietnameseText() {
        every { mockEngine.setLanguage(TtsSpeaker.LOCALE_VI) } returns TextToSpeech.LANG_AVAILABLE

        ttsSpeaker.speak("Đọc nhãn báo thức", useAlarmStream = false, locale = null)
        capturedListener?.onInit(TextToSpeech.SUCCESS)

        verify(exactly = 1) { mockEngine.setLanguage(TtsSpeaker.LOCALE_VI) }
        assertFalse(ttsSpeaker.isLanguageDataMissing.value)
    }
}
