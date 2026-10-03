package com.Lia.assistant.data

enum class LanguagePreference(val displayName: String, val description: String) {
    AUTO("Auto", "follows whatever you speak"),
    HINDI("Hindi", "always replies in Hindi"),
    HINGLISH("Hinglish", "Hindi in Roman letters, mixed with English"),
    ENGLISH("English", "always replies in English");

    companion object {
        fun fromName(name: String?): LanguagePreference =
            entries.firstOrNull { it.name == name } ?: AUTO
    }
}
