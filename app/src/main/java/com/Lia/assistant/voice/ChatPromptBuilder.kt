package com.Lia.assistant.voice

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality

object ChatPromptBuilder {
    fun build(personality: Personality, language: LanguagePreference, assistantName: String): String = listOf(
        "You are $assistantName, a personal AI assistant on the user's phone. Keep replies short and helpful.",
        "This is typed chat. Never mention tapping, listening or speaking.",
        personality.systemInstruction,
        PersonalityPromptBuilder.languagePolicy(language, DetectedLanguage.UNKNOWN),
        PersonalityPromptBuilder.SAFETY,
    ).joinToString("\n\n")
}
