package com.Lia.assistant.voice

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Mic 16 kHz in, speaker 24 kHz out. No logging in the audio path. */
class LiveAudioIO(
    private val onMicChunk: (ByteArray) -> Unit,
    private val onMicAmplitude: (Float) -> Unit,
) {
    companion object {
        const val MIC_RATE = 16000
        const val PLAY_RATE = 24000

        fun rms(pcm: ByteArray, len: Int = pcm.size): Float {
            val n = len / 2
            if (n == 0) return 0f
            var sum = 0.0
            for (i in 0 until n) {
                val s = ((pcm[2 * i + 1].toInt() shl 8) or (pcm[2 * i].toInt() and 0xFF)).toShort().toInt()
                sum += s.toDouble() * s
            }
            return min(1f, max(0f, (sqrt(sum / n) / 32768.0).toFloat()))
        }
    }

    @Volatile private var recording = false
    private var record: AudioRecord? = null
    private var micThread: Thread? = null
    private val effects = mutableListOf<android.media.audiofx.AudioEffect>()

    private val lock = Any()
    private var track: AudioTrack? = null
    private val queue = LinkedBlockingQueue<ByteArray>()
    private var playThread: Thread? = null
    @Volatile private var playing = false
    @Volatile private var volume = 1f

    @SuppressLint("MissingPermission")
    fun startMic(): Boolean {
        if (recording) return true
        val minBuf = AudioRecord.getMinBufferSize(MIC_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val rec = try {
            AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, MIC_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, max(minBuf, 4096))
        } catch (_: Exception) { return false }
        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); return false }
        val sid = rec.audioSessionId
        try { if (AcousticEchoCanceler.isAvailable()) AcousticEchoCanceler.create(sid)?.also { it.enabled = true; effects += it } } catch (_: Exception) {}
        try { if (NoiseSuppressor.isAvailable()) NoiseSuppressor.create(sid)?.also { it.enabled = true; effects += it } } catch (_: Exception) {}
        try { if (AutomaticGainControl.isAvailable()) AutomaticGainControl.create(sid)?.also { it.enabled = true; effects += it } } catch (_: Exception) {}
        rec.startRecording()
        record = rec
        recording = true
        micThread = Thread({
            val buf = ByteArray(2048)
            while (recording) {
                val n = rec.read(buf, 0, buf.size)
                if (n > 0) {
                    val chunk = buf.copyOf(n)
                    onMicAmplitude(rms(chunk, n))
                    onMicChunk(chunk)
                } else if (n < 0) break
            }
        }, "lia-mic").also { it.start() }
        return true
    }

    private fun ensureTrack(): AudioTrack = synchronized(lock) {
        track?.let { return it }
        val minBuf = AudioTrack.getMinBufferSize(PLAY_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setAudioFormat(AudioFormat.Builder()
                .setSampleRate(PLAY_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(minBuf * 4)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        t.setVolume(volume)
        t.play()
        track = t
        playing = true
        playThread = Thread({
            while (playing) {
                val chunk = try { queue.take() } catch (_: InterruptedException) { break }
                try { t.write(chunk, 0, chunk.size) } catch (_: Exception) {}
            }
        }, "lia-play").also { it.start() }
        t
    }

    /** Queues a PCM16 chunk and returns its RMS (0..1). */
    fun playChunk(bytes: ByteArray): Float {
        ensureTrack()
        queue.offer(bytes)
        return rms(bytes)
    }

    fun flushPlayback() {
        queue.clear()
        val t = synchronized(lock) { track } ?: return
        try { t.pause(); t.flush(); t.play() } catch (_: Exception) {}
    }

    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
        synchronized(lock) { track?.setVolume(volume) }
    }

    fun release() {
        recording = false
        playing = false
        playThread?.interrupt(); playThread = null
        queue.clear()
        try { record?.stop() } catch (_: Exception) {}
        try { record?.release() } catch (_: Exception) {}
        record = null
        effects.forEach { try { it.release() } catch (_: Exception) {} }
        effects.clear()
        synchronized(lock) {
            try { track?.stop() } catch (_: Exception) {}
            try { track?.release() } catch (_: Exception) {}
            track = null
        }
    }
}
