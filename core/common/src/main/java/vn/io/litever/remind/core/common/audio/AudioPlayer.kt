package vn.io.litever.remind.core.common.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class AudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var vibrator: Vibrator? = null
    private var focusRequest: AudioFocusRequest? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var volumeJob: Job? = null
    private var extraLoudJob: Job? = null

    private val onAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    if (isCallActive()) {
                        mediaPlayer?.pause()
                    }
                }

                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (mediaPlayer?.isPlaying == false) {
                        mediaPlayer?.start()
                    }
                }
            }
        }

    fun play(
        uri: Uri,
        useAlarmStream: Boolean = true,
        contentType: Int = AudioAttributes.CONTENT_TYPE_SONIFICATION,
        loop: Boolean = true,
        volume: Int? = null,
        gradualVolumeDurationSeconds: Int = 0,
        vibrationEnabled: Boolean = false,
        extraLoudEnabled: Boolean = false,
        extraLoudDelaySeconds: Int = 40
    ) {
        stop()

        // Don't start if a call is active or ringing
        if (isCallActive()) return

        val usage = if (useAlarmStream) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA

        val attributes = AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(contentType)
            .build()

        // Request Audio Focus
        val focusResult = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(attributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
            .build()
        focusRequest = focusResult
        audioManager.requestAudioFocus(focusResult)


        try {
            val player = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(attributes)
                isLooping = loop
            }

            mediaPlayer = player

            player.setOnPreparedListener { mp ->
                if (mediaPlayer != mp) {
                    mp.release()
                    return@setOnPreparedListener
                }
                mp.start()
                handleVolume(usage, volume, gradualVolumeDurationSeconds)
                if (extraLoudEnabled) {
                    scheduleExtraLoud(mp, usage, extraLoudDelaySeconds)
                }
                if (vibrationEnabled) startVibration()
            }
            player.prepareAsync()

        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    fun setVolume(usage: Int, volume: Int) {
        val streamType = usageToStreamType(usage)
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val volumeScale = volume.toFloat() / maxVolume.toFloat()

        mediaPlayer?.setVolume(volumeScale, volumeScale)
        audioManager.setStreamVolume(streamType, volume, 0)
    }

    private fun handleVolume(usage: Int, targetVolume: Int?, gradualDuration: Int) {
        val streamType = usageToStreamType(usage)
        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val finalVolume = targetVolume ?: audioManager.getStreamVolume(streamType)

        if (gradualDuration > 0) {
            volumeJob = scope.launch {
                val startTime = System.currentTimeMillis()
                val durationMs = gradualDuration * 1000L

                while (System.currentTimeMillis() - startTime < durationMs && isActive) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val currentVolume = 1 + ((finalVolume - 1) * elapsed / durationMs).toInt()
                    val volumeScale = currentVolume.toFloat() / maxVolume.toFloat()

                    mediaPlayer?.setVolume(volumeScale, volumeScale)
                    audioManager.setStreamVolume(streamType, currentVolume, 0)
                    delay(500.milliseconds)
                }

                // Ensure final volume is set
                val volumeScale = finalVolume.toFloat() / maxVolume.toFloat()
                mediaPlayer?.setVolume(volumeScale, volumeScale)
                audioManager.setStreamVolume(streamType, finalVolume, 0)
            }
        } else {
            val volumeScale = finalVolume.toFloat() / maxVolume.toFloat()
            mediaPlayer?.setVolume(volumeScale, volumeScale)
            audioManager.setStreamVolume(streamType, finalVolume, 0)
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 1000, 1000)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun scheduleExtraLoud(mp: MediaPlayer, usage: Int, delaySeconds: Int) {
        extraLoudJob = scope.launch {
            if (delaySeconds > 0) {
                delay(delaySeconds * 1000L)
            }
            if (mediaPlayer != mp) return@launch
            runCatching {
                val streamType = usageToStreamType(usage)
                val maxVol = audioManager.getStreamMaxVolume(streamType)
                audioManager.setStreamVolume(streamType, maxVol, 0)
                mp.setVolume(1f, 1f)
                loudnessEnhancer?.release()
                loudnessEnhancer = LoudnessEnhancer(mp.audioSessionId).apply {
                    setTargetGain(1200) // +12 dB boost
                    enabled = true
                }
            }
        }
    }

    fun stop() {
        volumeJob?.cancel()
        volumeJob = null
        extraLoudJob?.cancel()
        extraLoudJob = null

        runCatching {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
        }
        loudnessEnhancer = null

        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
            } catch (_: Exception) { }
            it.release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    }

    private fun isCallActive(): Boolean {
        return audioManager.mode in listOf(
            AudioManager.MODE_IN_CALL,
            AudioManager.MODE_IN_COMMUNICATION,
            AudioManager.MODE_RINGTONE
        )
    }

    private fun usageToStreamType(usage: Int): Int {
        return when (usage) {
            AudioAttributes.USAGE_ALARM -> AudioManager.STREAM_ALARM
            AudioAttributes.USAGE_NOTIFICATION_RINGTONE -> AudioManager.STREAM_RING
            else -> AudioManager.STREAM_MUSIC
        }
    }
}
