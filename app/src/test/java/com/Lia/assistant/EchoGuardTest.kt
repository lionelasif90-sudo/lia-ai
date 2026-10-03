package com.Lia.assistant

import com.Lia.assistant.voice.EchoGuardCore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EchoGuardTest {
    @Test fun mutesDuringPlaybackAndUnmutesAfterTail() = runTest {
        val g = EchoGuardCore(backgroundScope, tailMs = 450)
        g.notifyPlaybackStarted()
        assertTrue(g.isMicMuted())
        g.notifyPlaybackStopped()
        advanceTimeBy(440); runCurrent()
        assertTrue(g.isMicMuted())
        advanceTimeBy(20); runCurrent()
        assertFalse(g.isMicMuted())
    }

    @Test fun counterNeedsAllStops() = runTest {
        val g = EchoGuardCore(backgroundScope, tailMs = 450)
        g.notifyPlaybackStarted(); g.notifyPlaybackStarted()
        g.notifyPlaybackStopped()
        advanceTimeBy(1000); runCurrent()
        assertTrue(g.isMicMuted())
        g.notifyPlaybackStopped()
        advanceTimeBy(460); runCurrent()
        assertFalse(g.isMicMuted())
    }

    @Test fun watchdogForceUnmutes() = runTest {
        val g = EchoGuardCore(backgroundScope, tailMs = 450, watchdogMs = 60_000)
        g.notifyPlaybackStarted()
        advanceTimeBy(60_001); runCurrent()
        assertFalse(g.isMicMuted())
    }
}
