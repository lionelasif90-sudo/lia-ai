package com.Lia.assistant.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.Lia.assistant.ui.components.NovaOrbStyle
import com.Lia.assistant.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class ConversationSummary(val id: Long, val title: String, val updatedAt: Long)

/** Remembered once at the nav root. All writers are applyX (not setX) to avoid JVM setter clashes. */
class NovaAppState(context: Context) {
    private val ctx = context.applicationContext
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val K = NovaPreferences.Keys

    var themeMode by mutableStateOf(ThemeMode.fromName(NovaPreferences.getString(ctx, K.THEME_MODE, ThemeMode.DARK.name)))
        private set
    var reducedMotion by mutableStateOf(NovaPreferences.getBoolean(ctx, K.REDUCED_MOTION, false))
        private set
    var orbStyle by mutableStateOf(NovaOrbStyle.fromName(NovaPreferences.getString(ctx, K.ORB_STYLE, NovaOrbStyle.NOVA.name)))
        private set
    var personality by mutableStateOf(Personality.fromName(NovaPreferences.getString(ctx, K.PERSONALITY, Personality.DEFAULT.name)))
        private set
    var assistantName by mutableStateOf(NovaPreferences.getString(ctx, K.ASSISTANT_NAME, AssistantBrand.NAME).ifBlank { AssistantBrand.NAME })
        private set
    var userName by mutableStateOf(NovaPreferences.getString(ctx, K.USER_NAME, ""))
        private set
    var language by mutableStateOf(LanguagePreference.fromName(NovaPreferences.getString(ctx, K.LANGUAGE, LanguagePreference.AUTO.name)))
        private set
    var voiceName by mutableStateOf(NovaPreferences.getString(ctx, K.VOICE_NAME, PersonalityRepository.DEFAULT_VOICE))
        private set
    var speechSpeed by mutableStateOf(NovaPreferences.getFloat(ctx, K.SPEECH_SPEED, 1f))
        private set
    var voiceVolume by mutableStateOf(NovaPreferences.getFloat(ctx, K.VOICE_VOLUME, 1f))
        private set
    var hasOnboarded by mutableStateOf(NovaPreferences.getBoolean(ctx, K.HAS_ONBOARDED, false))
        private set

    val conversations = mutableStateListOf<ConversationSummary>()

    fun applyThemeMode(m: ThemeMode) { themeMode = m; NovaPreferences.putString(ctx, K.THEME_MODE, m.name) }
    fun applyReducedMotion(v: Boolean) { reducedMotion = v; NovaPreferences.putBoolean(ctx, K.REDUCED_MOTION, v) }
    fun applyOrbStyle(s: NovaOrbStyle) { orbStyle = s; NovaPreferences.putString(ctx, K.ORB_STYLE, s.name) }
    fun applyUserName(n: String) { userName = n.trim(); NovaPreferences.putString(ctx, K.USER_NAME, userName) }
    fun applySpeechSpeed(v: Float) { speechSpeed = v; NovaPreferences.putFloat(ctx, K.SPEECH_SPEED, v) }
    fun applyVoiceVolume(v: Float) {
        voiceVolume = v; NovaPreferences.putFloat(ctx, K.VOICE_VOLUME, v)
        com.Lia.assistant.voice.VoiceSessionManager.setVolume(v)
    }
    fun applyHasOnboarded(v: Boolean) { hasOnboarded = v; NovaPreferences.putBoolean(ctx, K.HAS_ONBOARDED, v) }

    fun applyAssistantName(n: String) {
        val clean = n.trim().ifEmpty { AssistantBrand.NAME }
        assistantName = clean
        io.launch { PersonalityRepository.setAssistantName(ctx, clean) }
    }
    fun applyLanguage(l: LanguagePreference) {
        language = l
        io.launch { PersonalityRepository.setLanguage(ctx, l) }
    }
    fun applyPersonality(p: Personality) {
        personality = p
        io.launch { PersonalityRepository.setPersonality(ctx, p) }
    }
    fun applyVoiceName(v: String) {
        voiceName = v
        io.launch { PersonalityRepository.setVoiceName(ctx, v) }
    }

    fun recordConversation(title: String) {
        val now = System.currentTimeMillis()
        conversations.add(0, ConversationSummary(now, title.take(60), now))
    }
}
