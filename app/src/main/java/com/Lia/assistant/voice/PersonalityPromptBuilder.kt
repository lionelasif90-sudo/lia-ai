package com.Lia.assistant.voice

import com.Lia.assistant.action.LiaCapabilityPrompts
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality

object PersonalityPromptBuilder {
    fun languagePolicy(language: LanguagePreference, detected: DetectedLanguage): String = when (language) {
        LanguagePreference.AUTO ->
            "Language: match the language of the user's latest turn (Hindi, Hinglish or English), turn by turn. " +
                "Never translate what they said. " + when (detected) {
                    DetectedLanguage.HINDI -> "They recently spoke Hindi."
                    DetectedLanguage.HINGLISH -> "They recently spoke Hinglish."
                    DetectedLanguage.ENGLISH -> "They recently spoke English."
                    DetectedLanguage.UNKNOWN -> ""
                }
        LanguagePreference.HINDI -> "Language: always reply in Hindi (Devanagari script), even if the user speaks another language."
        LanguagePreference.HINGLISH -> "Language: always reply in Hinglish (Hindi written in Roman letters mixed with English)."
        LanguagePreference.ENGLISH -> "Language: always reply in English."
    }.trim()

    const val SAFETY =
        "Safety: never claim to be human. No sexually explicit content. If the user asks to stop a topic, stop it. " +
            "Vary your phrasing; do not repeat the same sentences."

    fun build(
        personality: Personality,
        language: LanguagePreference,
        detectedLanguage: DetectedLanguage,
        recap: String?,
        assistantName: String,
    ): String {
        val parts = mutableListOf<String>()
        parts += "You are $assistantName, a persistent personal voice assistant living on the user's phone. " +
            "Keep replies short and conversational, like spoken conversation."
        parts += LiaCapabilityPrompts.toolInstruction(assistantName)
        parts += personality.systemInstruction
        parts += languagePolicy(language, detectedLanguage)
        parts += SAFETY
        if (recap != null) {
            parts += "This is a continuing conversation — do not greet again. Recap:\n$recap"
        }
        return parts.joinToString("\n\n")
    }
}
