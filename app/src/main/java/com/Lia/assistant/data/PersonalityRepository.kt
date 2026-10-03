package com.Lia.assistant.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.Lia.assistant.voice.VoiceSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.assistantDataStore by preferencesDataStore(name = "assistant_settings")

object PersonalityRepository {
    private val K_PERSONALITY = stringPreferencesKey("personality")
    private val K_LANGUAGE = stringPreferencesKey("language")
    private val K_NAME = stringPreferencesKey("assistant_name")
    private val K_VOICE = stringPreferencesKey("voice_name")

    const val DEFAULT_VOICE = "Aoede"

    // Pure fallbacks (unit-tested)
    internal fun resolvePersonality(stored: String?, fallback: String?): Personality =
        Personality.entries.firstOrNull { it.name == stored }
            ?: Personality.entries.firstOrNull { it.name == fallback }
            ?: Personality.DEFAULT

    internal fun resolveLanguage(stored: String?, fallback: String?): LanguagePreference =
        LanguagePreference.entries.firstOrNull { it.name == stored }
            ?: LanguagePreference.entries.firstOrNull { it.name == fallback }
            ?: LanguagePreference.AUTO

    internal fun resolveText(stored: String?, fallback: String?, default: String): String =
        stored?.trim()?.takeIf { it.isNotEmpty() } ?: fallback?.trim()?.takeIf { it.isNotEmpty() } ?: default

    fun personality(ctx: Context): Flow<Personality> = ctx.applicationContext.assistantDataStore.data.map {
        resolvePersonality(it[K_PERSONALITY], NovaPreferences.getString(ctx, NovaPreferences.Keys.PERSONALITY, ""))
    }

    fun language(ctx: Context): Flow<LanguagePreference> = ctx.applicationContext.assistantDataStore.data.map {
        resolveLanguage(it[K_LANGUAGE], NovaPreferences.getString(ctx, NovaPreferences.Keys.LANGUAGE, ""))
    }

    fun assistantName(ctx: Context): Flow<String> = ctx.applicationContext.assistantDataStore.data.map {
        resolveText(it[K_NAME], NovaPreferences.getString(ctx, NovaPreferences.Keys.ASSISTANT_NAME, ""), AssistantBrand.NAME)
    }

    fun voiceName(ctx: Context): Flow<String> = ctx.applicationContext.assistantDataStore.data.map {
        resolveText(it[K_VOICE], NovaPreferences.getString(ctx, NovaPreferences.Keys.VOICE_NAME, ""), DEFAULT_VOICE)
    }

    suspend fun getPersonality(ctx: Context) = personality(ctx).first()
    suspend fun getLanguage(ctx: Context) = language(ctx).first()
    suspend fun getAssistantName(ctx: Context) = assistantName(ctx).first()
    suspend fun getVoiceName(ctx: Context) = voiceName(ctx).first()

    suspend fun setPersonality(ctx: Context, p: Personality) {
        ctx.applicationContext.assistantDataStore.edit { it[K_PERSONALITY] = p.name }
        NovaPreferences.putString(ctx, NovaPreferences.Keys.PERSONALITY, p.name)
        VoiceSessionManager.refreshInstructions(ctx)
    }

    suspend fun setLanguage(ctx: Context, l: LanguagePreference) {
        ctx.applicationContext.assistantDataStore.edit { it[K_LANGUAGE] = l.name }
        NovaPreferences.putString(ctx, NovaPreferences.Keys.LANGUAGE, l.name)
        VoiceSessionManager.refreshInstructions(ctx)
    }

    suspend fun setAssistantName(ctx: Context, name: String) {
        val clean = name.trim().ifEmpty { AssistantBrand.NAME }
        ctx.applicationContext.assistantDataStore.edit { it[K_NAME] = clean }
        NovaPreferences.putString(ctx, NovaPreferences.Keys.ASSISTANT_NAME, clean)
        VoiceSessionManager.refreshInstructions(ctx)
    }

    suspend fun setVoiceName(ctx: Context, voice: String) {
        ctx.applicationContext.assistantDataStore.edit { it[K_VOICE] = voice }
        NovaPreferences.putString(ctx, NovaPreferences.Keys.VOICE_NAME, voice)
        VoiceSessionManager.refreshInstructions(ctx)
    }
}
