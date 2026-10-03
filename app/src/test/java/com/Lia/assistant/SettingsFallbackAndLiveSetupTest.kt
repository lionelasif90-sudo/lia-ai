package com.Lia.assistant

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality
import com.Lia.assistant.data.PersonalityRepository
import com.Lia.assistant.action.LiaToolCatalog
import com.Lia.assistant.voice.GeminiLiveClient
import com.Lia.assistant.voice.GeminiTextClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsFallbackAndLiveSetupTest {
    @Test fun personalityFallbacks() {
        assertEquals(Personality.FUNNY, PersonalityRepository.resolvePersonality("FUNNY", "TEACHER"))
        assertEquals(Personality.TEACHER, PersonalityRepository.resolvePersonality(null, "TEACHER"))
        assertEquals(Personality.NORMAL, PersonalityRepository.resolvePersonality("bogus", ""))
    }

    @Test fun languageAndNameFallbacks() {
        assertEquals(LanguagePreference.AUTO, PersonalityRepository.resolveLanguage(null, null))
        assertEquals(LanguagePreference.HINDI, PersonalityRepository.resolveLanguage(null, "HINDI"))
        assertEquals("Lia", PersonalityRepository.resolveText("  ", null, "Lia"))
        assertEquals("Zoya", PersonalityRepository.resolveText(null, "Zoya", "Lia"))
    }

    @Test fun liveSetupHasRequiredFieldsAndNoVadOverride() {
        val s = GeminiLiveClient.buildSetup(GeminiLiveClient.MODEL, "hello", "Aoede", LiaToolCatalog.declarations()).toString()
        assertTrue(s.contains("\"responseModalities\":[\"AUDIO\"]"))
        assertTrue(s.contains("inputAudioTranscription")); assertTrue(s.contains("outputAudioTranscription"))
        assertTrue(s.contains("open_app"))
        assertFalse(s.contains("automaticActivityDetection"))
    }

    @Test fun textModelSelection() {
        val json = """{"models":[
            {"name":"models/gemini-live-x","supportedGenerationMethods":["generateContent"]},
            {"name":"models/gemini-flash-image","supportedGenerationMethods":["generateContent"]},
            {"name":"models/gemini-pro-1","supportedGenerationMethods":["generateContent"]},
            {"name":"models/gemini-flash-2","supportedGenerationMethods":["generateContent"]}]}"""
        assertEquals("models/gemini-flash-2", GeminiTextClient.chooseModel(json))
        val noFlash = """{"models":[{"name":"models/gemini-pro-1","supportedGenerationMethods":["generateContent"]}]}"""
        assertEquals("models/gemini-pro-1", GeminiTextClient.chooseModel(noFlash))
    }
}
