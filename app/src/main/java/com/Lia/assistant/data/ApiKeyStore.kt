package com.Lia.assistant.data

import android.content.Context

/** The Gemini key is typed by the user in Settings and stays on this device. Never log it. */
object ApiKeyStore {
    private const val PREFS = "nova_prefs"
    private const val KEY = "gemini_api_key"

    fun getKey(ctx: Context): String =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "").orEmpty().trim()

    fun saveKey(ctx: Context, key: String) {
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, key.trim()).apply()
    }

    fun hasKey(ctx: Context): Boolean = getKey(ctx).isNotEmpty()
}
