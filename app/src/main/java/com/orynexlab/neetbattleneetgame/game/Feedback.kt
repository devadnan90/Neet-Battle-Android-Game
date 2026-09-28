package com.orynexlab.neetbattleneetgame.game

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Tones are synthesized at runtime rather than shipped as assets, so the whole
 * audio layer costs zero KB in the APK.
 */
object Feedback {

    private const val RATE = 22050
    private var vibrator: Vibrator? = null
    var enabled = true

    fun init(ctx: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= 31) {
            (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /** freqs played in sequence; decay shapes the envelope. */
    private fun tone(freqs: List<Float>, durMs: Int, vol: Float = 0.32f, decay: Float = 6f) {
        if (!enabled) return
        thread(isDaemon = true) {
            try {
                val perNote = durMs / freqs.size
                val n = RATE * perNote / 1000
                val buf = ShortArray(n * freqs.size)
                var idx = 0
                freqs.forEach { f ->
                    for (i in 0 until n) {
                        val t = i.toFloat() / RATE
                        val env = exp(-decay * t / (perNote / 1000f))
                        // slight second harmonic keeps it from sounding like a test beep
                        val s = sin(2 * PI * f * t) * 0.8 + sin(4 * PI * f * t) * 0.2
                        buf[idx++] = (s * env * vol * Short.MAX_VALUE).toInt().toShort()
                    }
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(RATE)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buf.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buf, 0, buf.size)
                track.play()
                Thread.sleep((durMs + 120).toLong())
                track.release()
            } catch (_: Exception) { }
        }
    }

    private fun buzz(ms: Long, amp: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (!enabled) return
        try {
            vibrator?.vibrate(VibrationEffect.createOneShot(ms, amp))
        } catch (_: Exception) { }
    }

    private fun pattern(timings: LongArray, amps: IntArray) {
        if (!enabled) return
        try {
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amps, -1))
        } catch (_: Exception) { }
    }

    fun tap() { tone(listOf(660f), 45, 0.18f, 12f); buzz(12, 60) }

    /** rising third — reward */
    fun correct(streak: Int) {
        val base = 523f * (1f + (streak.coerceAtMost(5) * 0.06f))
        tone(listOf(base, base * 1.26f, base * 1.5f), 210, 0.30f, 5f)
        buzz(28, 140)
    }

    /** falling minor second — the sound of getting it wrong */
    fun wrong() {
        tone(listOf(300f, 220f), 240, 0.28f, 4f)
        pattern(longArrayOf(0, 45, 55, 65), intArrayOf(0, 190, 0, 130))
    }

    fun timeout() { tone(listOf(240f, 190f, 150f), 320, 0.26f, 3.5f); buzz(90, 150) }

    /** last-seconds tick */
    fun tick() { tone(listOf(1180f), 32, 0.14f, 16f) }

    fun countdown(n: Int) {
        tone(listOf(if (n == 1) 880f else 520f), if (n == 1) 200 else 110, 0.26f, 9f)
        buzz(if (n == 1) 40 else 18, 120)
    }

    fun win() {
        tone(listOf(523f, 659f, 784f, 1047f), 520, 0.32f, 3f)
        pattern(longArrayOf(0, 60, 60, 60, 60, 140), intArrayOf(0, 160, 0, 190, 0, 255))
    }

    fun lose() {
        tone(listOf(440f, 370f, 294f), 520, 0.28f, 3f)
        buzz(180, 120)
    }

    fun levelUp() {
        tone(listOf(659f, 784f, 988f, 1319f, 1568f), 620, 0.34f, 2.5f)
        pattern(longArrayOf(0, 50, 40, 50, 40, 200), intArrayOf(0, 140, 0, 190, 0, 255))
    }
}
