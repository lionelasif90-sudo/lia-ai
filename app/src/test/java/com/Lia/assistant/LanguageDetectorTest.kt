package com.Lia.assistant

import com.Lia.assistant.voice.DetectedLanguage
import com.Lia.assistant.voice.LanguageDetector
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageDetectorTest {
    @Test fun devanagari() = assertEquals(DetectedLanguage.HINDI, LanguageDetector.detect("आप कैसे हैं"))
    @Test fun hinglish() = assertEquals(DetectedLanguage.HINGLISH, LanguageDetector.detect("kya haal hai yaar"))
    @Test fun english() = assertEquals(DetectedLanguage.ENGLISH, LanguageDetector.detect("what's the weather"))
    @Test fun blank() = assertEquals(DetectedLanguage.UNKNOWN, LanguageDetector.detect("   "))
    @Test fun longEnglishStaysEnglish() =
        assertEquals(DetectedLanguage.ENGLISH, LanguageDetector.detect("please open the settings app and turn on the flashlight for me"))
    @Test fun longHinglish() =
        assertEquals(DetectedLanguage.HINGLISH, LanguageDetector.detect("mujhe batao ki aaj mausam kaisa rahega yaar"))
}
