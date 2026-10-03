package com.Lia.assistant.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LatencySnapshot(
    val responseStartMs: Long? = null,
    val firstAudioMs: Long? = null,
    val turnDurationMs: Long? = null,
)

class VoiceLatencyTracker(private val clock: () -> Long = { System.currentTimeMillis() }) {
    enum class Mark { SPEECH_DETECTED, RESPONSE_STARTED, AUDIO_STARTED, TURN_COMPLETE }

    private val stamps = LongArray(Mark.entries.size) { -1L }
    private var last = -1
    private val _snapshot = MutableStateFlow(LatencySnapshot())
    val snapshot: StateFlow<LatencySnapshot> = _snapshot.asStateFlow()
    private val _reconnectCount = MutableStateFlow(0)
    val reconnectCount: StateFlow<Int> = _reconnectCount.asStateFlow()

    @Synchronized
    fun mark(m: Mark) {
        if (m == Mark.SPEECH_DETECTED && last == Mark.TURN_COMPLETE.ordinal) {
            stamps.fill(-1L); last = -1
        }
        if (m.ordinal <= last) return // out-of-order or duplicate marks are ignored
        stamps[m.ordinal] = clock()
        last = m.ordinal
        _snapshot.value = compute()
    }

    private fun delta(from: Mark, to: Mark): Long? {
        val a = stamps[from.ordinal]; val b = stamps[to.ordinal]
        return if (a >= 0 && b >= 0) b - a else null
    }

    private fun compute() = LatencySnapshot(
        responseStartMs = delta(Mark.SPEECH_DETECTED, Mark.RESPONSE_STARTED),
        firstAudioMs = delta(Mark.SPEECH_DETECTED, Mark.AUDIO_STARTED),
        turnDurationMs = delta(Mark.SPEECH_DETECTED, Mark.TURN_COMPLETE),
    )

    fun reconnected() { _reconnectCount.value = _reconnectCount.value + 1 }
}
