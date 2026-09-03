package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object TechSoundManager {
    private const val TAG = "TechSoundManager"
    private const val SAMPLE_RATE = 44100

    /**
     * Plays a very brief (110ms) subtle futuristic cyber activation ping when wake-word triggers.
     */
    suspend fun playWakeTriggerSound(context: Context) {
        withContext(Dispatchers.Default) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                if (audioManager != null && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                    return@withContext
                }
                val pcmData = generateWakeSoundWave()
                playPcmAudio(pcmData)
            } catch (e: Exception) {
                Log.d(TAG, "Skipping wake sound: ${e.message}")
            }
        }
    }

    /**
     * Plays a fast (60ms) subtle digital click when thinking / reasoning begins.
     */
    suspend fun playThinkingChirp(context: Context) {
        withContext(Dispatchers.Default) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                if (audioManager != null && audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                    return@withContext
                }
                val pcmData = generateThinkingChirpWave()
                playPcmAudio(pcmData)
            } catch (e: Exception) {
                Log.d(TAG, "Skipping thinking chirp: ${e.message}")
            }
        }
    }

    private fun generateWakeSoundWave(): ShortArray {
        val totalDurationMs = 120
        val totalSamples = (SAMPLE_RATE * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 1200.0 + (t / 0.12) * 800.0 // 1200Hz -> 2000Hz rising chirp
            val env = exp(-t * 28.0)
            val sine = sin(2.0 * PI * freq * t)
            val harmonic = 0.25 * sin(2.0 * PI * (freq * 1.5) * t)
            val sample = (sine + harmonic) * env * 0.45
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    private fun generateThinkingChirpWave(): ShortArray {
        val totalDurationMs = 60
        val totalSamples = (SAMPLE_RATE * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 2400.0
            val env = exp(-t * 60.0)
            val sine = sin(2.0 * PI * freq * t)
            val sample = sine * env * 0.3
            buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    /**
     * Plays a modern, futuristic 3-second Cyber-AI initialization chime synthesized dynamically.
     * Zero external file dependency - generated on-the-fly via mathematical PCM synthesis.
     *
     * Tone Structure:
     * - 0.0s - 1.0s: Deep, rich sub-bass electronic rise (120Hz -> 440Hz sine sweep).
     * - 1.0s - 2.2s: Futuristic harmonic pulse / neural digital chime (dual 528Hz & 880Hz with reverb resonance).
     * - 2.2s - 3.0s: Crisp metallic cyber-lock completion ping (high-frequency soft exponential decay).
     */
    suspend fun playCyberStartupChime(context: Context) {
        withContext(Dispatchers.Default) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                if (audioManager != null) {
                    // Respect silent or vibrate modes
                    if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
                        Log.d(TAG, "Device is in silent/vibrate mode. Skipping sound.")
                        return@withContext
                    }
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (currentVolume == 0) {
                        Log.d(TAG, "Media volume is muted. Skipping sound.")
                        return@withContext
                    }
                }

                val pcmData = generateCyberAiStartupChime()
                playPcmAudio(pcmData)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play Cyber-AI startup chime", e)
            }
        }
    }

    /**
     * Backward-compatible alias for startup sound
     */
    suspend fun playHackingBootSound(context: Context) {
        playCyberStartupChime(context)
    }

    /**
     * Generates a 3.0-second high-tech synthesized Cyber-AI initialization chime:
     * - Section 1 (0.0s - 1.0s): Sub-bass electronic rise (120Hz to 440Hz continuous sine sweep)
     * - Section 2 (1.0s - 2.2s): Dual harmonic pulse & neural chime (528Hz + 880Hz with triangle/sine blend & reverb)
     * - Section 3 (2.2s - 3.0s): Metallic cyber-lock ping (1760Hz + 2640Hz + 3520Hz with exponential decay fade)
     */
    private fun generateCyberAiStartupChime(): ShortArray {
        val totalDurationMs = 3000
        val totalSamples = (SAMPLE_RATE * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        // Reverberation delay buffer (50ms delay line for simulated cyber acoustic space)
        val reverbDelaySamples = (SAMPLE_RATE * 0.05).toInt()
        val drySignal = DoubleArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE // 0.0 to 3.0 seconds
            var sample = 0.0

            if (t < 1.0) {
                // --- SECTION 1 (0.0s - 1.0s): Sub-Bass Electronic Rise (120Hz -> 440Hz) ---
                val progress = t / 1.0
                // Phase-accurate sweep from 120Hz to 440Hz
                val phase = 2.0 * PI * (120.0 * t + (320.0 / 2.0) * (t * t))
                val subPhase = 2.0 * PI * (60.0 * t + (160.0 / 2.0) * (t * t)) // 60Hz -> 220Hz warm sub octave

                // Smooth fade-in attack (0 to 0.18s) to prevent audio click
                val attackEnv = (t / 0.18).coerceIn(0.0, 1.0)
                // Slight harmonic warmth
                val fundamental = sin(phase)
                val subBass = 0.45 * sin(subPhase)
                val secondHarmonic = 0.2 * sin(phase * 2.0)

                // Envelope grows in presence up to 1.0s
                val presenceEnv = 0.6 + 0.4 * progress
                sample = (fundamental * 0.65 + subBass + secondHarmonic) * attackEnv * presenceEnv * 0.75
            } else if (t < 2.2) {
                // --- SECTION 2 (1.0s - 2.2s): Harmonic Pulse / Neural Digital Chime (528Hz & 880Hz) ---
                val localT = t - 1.0 // 0.0 to 1.2s
                val pulseEnv = sin(PI * (localT / 1.2)).coerceAtLeast(0.0) // smooth arch envelope
                val sustainEnv = exp(-localT * 0.85)

                // 528Hz (Solfeggio Transformation tone) + 880Hz (A5 Neural Chime)
                val f1 = 528.0
                val f2 = 880.0
                val f3 = 1056.0 // 528 * 2 harmonic

                // Sine tones + Triangle tone blend for futuristic digital timbre
                val sine1 = sin(2.0 * PI * f1 * localT)
                val sine2 = sin(2.0 * PI * f2 * localT)
                val triangle1 = (2.0 / PI) * Math.asin(sin(2.0 * PI * f1 * localT).coerceIn(-1.0, 1.0))
                val harmonic3 = 0.25 * sin(2.0 * PI * f3 * localT)

                // Subtle spatial chorus shimmer modulation (3.5Hz LFO)
                val shimmer = 1.0 + 0.15 * sin(2.0 * PI * 3.5 * localT)

                // Lingering tail from section 1 sub-bass resolving into 440Hz foundation
                val tailSub = 0.25 * sin(2.0 * PI * 220.0 * localT) * exp(-localT * 3.0)

                sample = ((sine1 * 0.4 + triangle1 * 0.25 + sine2 * 0.35 + harmonic3) * shimmer + tailSub) * (pulseEnv * 0.5 + sustainEnv * 0.5) * 0.8
            } else {
                // --- SECTION 3 (2.2s - 3.0s): Metallic Cyber-Lock Ping (1760Hz, 2640Hz, 3520Hz) ---
                val localT = t - 2.2 // 0.0 to 0.8s
                // Fast metallic impact attack followed by smooth exponential decay
                val decayEnv = exp(-localT * 4.2)

                val bell1 = sin(2.0 * PI * 1760.0 * localT) // A6
                val bell2 = 0.55 * sin(2.0 * PI * 2640.0 * localT) // E7
                val bell3 = 0.3 * sin(2.0 * PI * 3520.0 * localT) // A7 high sparkle
                val subRing = 0.2 * sin(2.0 * PI * 880.0 * localT) // Lingering warmth

                // High-pass metallic shimmer click at inception (first 30ms)
                val click = if (localT < 0.03) (0.25 * sin(2.0 * PI * 4800.0 * localT)) else 0.0

                sample = (bell1 * 0.5 + bell2 + bell3 + subRing + click) * decayEnv * 0.75
            }

            drySignal[i] = sample
        }

        // Apply Simulated Reverb (comb filtering + soft feedback blend)
        for (i in 0 until totalSamples) {
            var finalSample = drySignal[i]
            if (i >= reverbDelaySamples) {
                finalSample += 0.22 * drySignal[i - reverbDelaySamples]
            }
            if (i >= reverbDelaySamples * 2) {
                finalSample += 0.10 * drySignal[i - reverbDelaySamples * 2]
            }

            val clamped = finalSample.coerceIn(-1.0, 1.0)
            buffer[i] = (clamped * Short.MAX_VALUE * 0.8).toInt().toShort()
        }

        return buffer
    }

    private fun playPcmAudio(pcmData: ShortArray) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = maxOf(minBufferSize, pcmData.size * 2)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            audioTrack.play()
            audioTrack.write(pcmData, 0, pcmData.size)
            // Wait briefly for playback to finish before releasing
            val sleepTimeMs = (pcmData.size * 1000L) / SAMPLE_RATE + 100L
            Thread.sleep(sleepTimeMs)
            audioTrack.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing AudioTrack stream", e)
        } finally {
            audioTrack.release()
        }
    }
}
