package com.Lia.assistant

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality
import com.Lia.assistant.voice.ConversationMemory
import com.Lia.assistant.voice.DetectedLanguage
import com.Lia.assistant.voice.PersonalityPromptBuilder
import com.Lia.assistant.voice.Role
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {
    @Test fun everyPersonalityAndLanguageBuilds() {
        for (p in Personality.entries) for (l in LanguagePreference.entries) {
            val s = PersonalityPromptBuilder.build(p, l, DetectedLanguage.UNKNOWN, null, "Lia")
            assertTrue(s.contains("You are Lia"))
            assertTrue(s.contains(p.systemInstruction))
            assertFalse(s.contains("continuing conversation"))
        }
    }

    @Test fun recapOnlyWhenProvided() {
        val s = PersonalityPromptBuilder.build(Personality.NORMAL, LanguagePreference.AUTO, DetectedLanguage.HINGLISH, "User: hi", "Lia")
        assertTrue(s.contains("do not greet again"))
        assertTrue(s.contains("User: hi"))
    }

    @Test fun companionModesKeepSafetyRules() {
        for (p in listOf(Personality.COMPANION, Personality.GF_MODE)) {
            assertTrue(p.systemInstruction.contains("Never claim to be human"))
            assertTrue(p.systemInstruction.contains("No sexually explicit content"))
        }
    }

    @Test fun memoryKeepsLast12AndCapsRecap() {
        ConversationMemory.clear()
        repeat(20) { ConversationMemory.add(if (it % 2 == 0) Role.USER else Role.ASSISTANT, "turn $it " + "x".repeat(200)) }
        val r = ConversationMemory.recap()
        assertTrue(r.length <= 1600)
        assertFalse(r.contains("turn 0 "))
        assertTrue(r.contains("turn 19"))
        ConversationMemory.clear()
    }
}
