package com.Lia.assistant.data

import android.content.Context

object NovaPreferences {
    private const val PREFS = "nova_prefs"

    object Keys {
        const val THEME_MODE = "theme_mode"
        const val REDUCED_MOTION = "reduced_motion"
        const val ORB_STYLE = "orb_style"
        const val PERSONALITY = "personality"
        const val USER_NAME = "user_name"
        const val VOICE_INPUT_ENABLED = "voice_input_enabled"
        const val WAKE_WORD_ENABLED = "wake_word_enabled"
        const val LANGUAGE = "language"
        const val HAS_ONBOARDED = "has_onboarded"
        const val VOICE_NAME = "voice_name"
        const val RESPONSE_STYLE = "response_style"
        const val SPEECH_SPEED = "speech_speed"
        const val VOICE_VOLUME = "voice_volume"
        const val ASSISTANT_NAME = "assistant_name"
    }

    private fun p(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getString(ctx: Context, key: String, default: String): String =
        p(ctx).getString(key, default) ?: default

    fun putString(ctx: Context, key: String, value: String) {
        p(ctx).edit().putString(key, value).apply()
    }

    fun getBoolean(ctx: Context, key: String, default: Boolean): Boolean =
        p(ctx).getBoolean(key, default)

    fun putBoolean(ctx: Context, key: String, value: Boolean) {
        p(ctx).edit().putBoolean(key, value).apply()
    }

    fun getFloat(ctx: Context, key: String, default: Float): Float =
        p(ctx).getFloat(key, default)

    fun putFloat(ctx: Context, key: String, value: Float) {
        p(ctx).edit().putFloat(key, value).apply()
    }
}
