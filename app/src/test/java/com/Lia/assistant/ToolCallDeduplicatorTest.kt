package com.Lia.assistant

import com.Lia.assistant.voice.ToolCallDeduplicator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolCallDeduplicatorTest {
    @Test fun skipsRepeats() {
        val d = ToolCallDeduplicator()
        assertTrue(d.firstTime("a")); assertFalse(d.firstTime("a")); assertTrue(d.firstTime("b"))
    }
    @Test fun isBounded() {
        val d = ToolCallDeduplicator(maxSize = 3)
        listOf("1", "2", "3", "4").forEach { d.firstTime(it) }
        assertTrue(d.firstTime("1")) // evicted, so it counts as new again
    }
}
