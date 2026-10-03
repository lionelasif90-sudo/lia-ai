package com.Lia.assistant.voice

enum class DetectedLanguage { HINDI, HINGLISH, ENGLISH, UNKNOWN }

object LanguageDetector {
    private val HINGLISH_MARKERS = setOf(
        "hai", "hain", "ho", "kya", "kyun", "kaise", "nahi", "haan", "mujhe", "tumhe", "aap", "mera",
        "yaar", "bhai", "accha", "theek", "matlab", "karna", "karo", "raha", "chahiye", "batao",
        "suno", "dekho", "abhi", "phir", "lekin", "bahut", "thoda", "kuch", "sab", "wala",
        "kar", "mein", "main", "tum", "hum", "nahin", "kaisa", "kaun", "kab", "kahan",
    )
    private val splitter = Regex("[^a-zA-Z']+")

    fun detect(text: String): DetectedLanguage {
        if (text.isBlank()) return DetectedLanguage.UNKNOWN
        if (text.any { it.code in 0x0900..0x097F }) return DetectedLanguage.HINDI
        val words = text.lowercase().split(splitter).filter { it.isNotEmpty() }
        if (words.isEmpty()) return DetectedLanguage.UNKNOWN
        val hits = words.count { it in HINGLISH_MARKERS }
        return if (words.size <= 4) {
            if (hits >= 1) DetectedLanguage.HINGLISH else DetectedLanguage.ENGLISH
        } else {
            if (hits >= 2 || hits.toFloat() / words.size >= 0.15f) DetectedLanguage.HINGLISH
            else DetectedLanguage.ENGLISH
        }
    }
}
