package com.Lia.assistant

import com.Lia.assistant.voice.VoiceLatencyTracker
import com.Lia.assistant.voice.VoiceLatencyTracker.Mark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceLatencyTrackerTest {
    @Test fun inOrderMarksGiveDeltas() {
        var now = 1000L
        val t = VoiceLatencyTracker { now }
        t.mark(Mark.SPEECH_DETECTED); now = 1300; t.mark(Mark.RESPONSE_STARTED)
        now = 1450; t.mark(Mark.AUDIO_STARTED); now = 2000; t.mark(Mark.TURN_COMPLETE)
        val s = t.snapshot.value
        assertEquals(300L, s.responseStartMs); assertEquals(450L, s.firstAudioMs); assertEquals(1000L, s.turnDurationMs)
    }

    @Test fun outOfOrderIgnored() {
        var now = 0L
        val t = VoiceLatencyTracker { now }
        t.mark(Mark.SPEECH_DETECTED); now = 500; t.mark(Mark.AUDIO_STARTED)
        now = 900; t.mark(Mark.RESPONSE_STARTED) // earlier stage after a later one: ignored
        assertNull(t.snapshot.value.responseStartMs)
        assertEquals(500L, t.snapshot.value.firstAudioMs)
    }
}
