package com.Lia.assistant.voice

import android.content.Context
import com.Lia.assistant.action.ActionExecutor
import com.Lia.assistant.action.InstalledAppLabelCache
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.NovaPreferences
import com.Lia.assistant.data.Personality
import com.Lia.assistant.data.PersonalityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

/** Process-wide voice session, independent of any screen. */
object VoiceSessionManager {
    private const val MAX_RECONNECTS = 5
    private const val RECONNECT_DELAY_MS = 1200L
    private const val PLAYBACK_SILENCE_MS = 300L
    private val VOICES = setOf("Aoede", "Puck", "Charon", "Kore", "Fenrir", "Leda", "Orus", "Zephyr")

    private val _state = MutableStateFlow(NovaOrbState.IDLE)
    val state: StateFlow<NovaOrbState> = _state.asStateFlow()
    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()
    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted.asStateFlow()
    private val _statusText = MutableStateFlow("")
    val statusText: StateFlow<String> = _statusText.asStateFlow()
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    private val _currentPersonality = MutableStateFlow(Personality.DEFAULT)
    val currentPersonality: StateFlow<Personality> = _currentPersonality.asStateFlow()
    private val _currentLanguage = MutableStateFlow(LanguagePreference.AUTO)
    val currentLanguage: StateFlow<LanguagePreference> = _currentLanguage.asStateFlow()
    private val _detectedLanguage = MutableStateFlow(DetectedLanguage.UNKNOWN)
    val detectedLanguage: StateFlow<DetectedLanguage> = _detectedLanguage.asStateFlow()
    private val _vadActive = MutableStateFlow(false)
    val vadActive: StateFlow<Boolean> = _vadActive.asStateFlow()

    val latency = VoiceLatencyTracker()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val epoch = AtomicInteger(0)
    private val reconnectLock = Any()

    @Volatile private var appContext: Context? = null
    @Volatile private var client: GeminiLiveClient? = null
    @Volatile private var audio: LiveAudioIO? = null
    @Volatile private var micStarted = false
    @Volatile private var playing = false
    @Volatile private var assistantName = AssistantBrand.NAME
    private var reconnectAttempts = 0
    private var reconnectJob: Job? = null
    private var silenceJob: Job? = null

    private val userBuf = StringBuilder()
    private val modelBuf = StringBuilder()

    private fun setState(s: NovaOrbState) {
        _state.value = s
        _statusText.value = when (s) {
            NovaOrbState.IDLE -> "Tap to start"
            NovaOrbState.LISTENING -> "$assistantName is listening..."
            NovaOrbState.THINKING -> "$assistantName is thinking..."
            NovaOrbState.SPEAKING -> "$assistantName is speaking..."
            NovaOrbState.CONNECTING -> "Connecting..."
            NovaOrbState.ERROR -> _lastError.value ?: "Something went wrong"
        }
    }

    fun start(ctx: Context) {
        if (_isActive.value) return
        val app = ctx.applicationContext
        appContext = app
        _lastError.value = null
        if (!ApiKeyStore.hasKey(app)) {
            _lastError.value = "Add your Gemini API key in Settings first"
            setState(NovaOrbState.ERROR)
            return
        }
        ConversationMemory.clear()
        reconnectAttempts = 0
        micStarted = false
        playing = false
        _muted.value = false
        _amplitude.value = 0f
        EchoGuard.reset()
        InstalledAppLabelCache.prewarm(app)
        audio = LiveAudioIO(
            onMicChunk = { bytes ->
                if (!_muted.value && !EchoGuard.isMicMuted()) client?.sendAudio(bytes)
            },
            onMicAmplitude = { rms ->
                _vadActive.value = rms > 0.03f && !_muted.value && !EchoGuard.isMicMuted()
                if (_state.value == NovaOrbState.LISTENING) _amplitude.value = rms
            },
        ).also { it.setVolume(NovaPreferences.getFloat(app, NovaPreferences.Keys.VOICE_VOLUME, 1f)) }
        _isActive.value = true
        setState(NovaOrbState.CONNECTING)
        scope.launch { mutex.withLock { connectSession(isResuming = false) } }
    }

    private suspend fun connectSession(isResuming: Boolean) {
        val ctx = appContext ?: return
        val myEpoch = epoch.incrementAndGet()
        client?.disconnect()
        val personality = PersonalityRepository.getPersonality(ctx)
        val language = PersonalityRepository.getLanguage(ctx)
        val name = PersonalityRepository.getAssistantName(ctx)
        val voice = PersonalityRepository.getVoiceName(ctx)
        assistantName = name
        _currentPersonality.value = personality
        _currentLanguage.value = language
        val recap = if (isResuming) ConversationMemory.recap().takeIf { it.isNotBlank() } else null
        val prompt = PersonalityPromptBuilder.build(personality, language, _detectedLanguage.value, recap, name)
        if (epoch.get() != myEpoch || !_isActive.value) return
        setState(NovaOrbState.CONNECTING)
        val c = GeminiLiveClient(ApiKeyStore.getKey(ctx), listenerFor(myEpoch))
        client = c
        c.connect(prompt, mapVoice(voice))
    }

    private fun mapVoice(ui: String): String = if (ui in VOICES) ui else "Aoede"

    private fun listenerFor(myEpoch: Int) = object : GeminiLiveClient.Listener {
        private fun current() = epoch.get() == myEpoch && _isActive.value

        override fun onSetupComplete() {
            if (!current()) return
            reconnectAttempts = 0
            _lastError.value = null
            setState(NovaOrbState.LISTENING)
            if (!micStarted) {
                micStarted = audio?.startMic() == true
                if (!micStarted) {
                    _lastError.value = "Microphone unavailable"
                    setState(NovaOrbState.ERROR)
                }
            }
        }

        override fun onAudioChunk(bytes: ByteArray) {
            if (!current()) return
            if (!playing) {
                playing = true
                EchoGuard.notifyPlaybackStarted()
                latency.mark(VoiceLatencyTracker.Mark.RESPONSE_STARTED)
                latency.mark(VoiceLatencyTracker.Mark.AUDIO_STARTED)
            }
            _amplitude.value = audio?.playChunk(bytes) ?: 0f
            if (_state.value != NovaOrbState.SPEAKING) setState(NovaOrbState.SPEAKING)
            silenceJob?.cancel()
            silenceJob = scope.launch {
                delay(PLAYBACK_SILENCE_MS)
                if (epoch.get() == myEpoch) endPlayback()
            }
        }

        override fun onInputTranscript(text: String) {
            if (!current()) return
            synchronized(userBuf) {
                if (modelBuf.isNotEmpty()) commitModel()
                if (userBuf.isEmpty()) latency.mark(VoiceLatencyTracker.Mark.SPEECH_DETECTED)
                userBuf.append(text)
                _detectedLanguage.value = LanguageDetector.detect(userBuf.toString())
                    .takeIf { it != DetectedLanguage.UNKNOWN } ?: _detectedLanguage.value
            }
            if (_state.value == NovaOrbState.LISTENING) setState(NovaOrbState.THINKING)
        }

        override fun onOutputTranscript(text: String) {
            if (!current()) return
            synchronized(userBuf) {
                if (userBuf.isNotEmpty()) commitUser()
                if (modelBuf.isEmpty()) latency.mark(VoiceLatencyTracker.Mark.RESPONSE_STARTED)
                modelBuf.append(text)
            }
        }

        override fun onToolCall(id: String, name: String, args: JSONObject) {
            if (!current()) return
            val ctx = appContext ?: return
            scope.launch {
                val result = try {
                    ActionExecutor.execute(ctx, name, args)
                } catch (_: Exception) {
                    JSONObject().put("error", "tool_failed")
                }
                if (epoch.get() == myEpoch) client?.sendToolResponse(id, name, result)
            }
        }

        override fun onTurnComplete() {
            if (!current()) return
            synchronized(userBuf) { commitUser(); commitModel() }
            latency.mark(VoiceLatencyTracker.Mark.TURN_COMPLETE)
            endPlayback()
        }

        override fun onInterrupted() {
            if (!current()) return
            audio?.flushPlayback()
            synchronized(userBuf) { commitModel() }
            latency.mark(VoiceLatencyTracker.Mark.TURN_COMPLETE)
            endPlayback()
        }

        override fun onError(message: String) {
            if (epoch.get() == myEpoch && _isActive.value) scheduleReconnect()
        }

        override fun onClosed() {
            if (epoch.get() == myEpoch && _isActive.value) scheduleReconnect()
        }
    }

    private fun commitUser() {
        if (userBuf.isNotEmpty()) { ConversationMemory.add(Role.USER, userBuf.toString()); userBuf.setLength(0) }
    }

    private fun commitModel() {
        if (modelBuf.isNotEmpty()) { ConversationMemory.add(Role.ASSISTANT, modelBuf.toString()); modelBuf.setLength(0) }
    }

    private fun endPlayback() {
        silenceJob?.cancel()
        if (playing) {
            playing = false
            EchoGuard.notifyPlaybackStopped()
        }
        _amplitude.value = 0f
        if (_isActive.value && _state.value != NovaOrbState.ERROR && _state.value != NovaOrbState.CONNECTING) {
            setState(NovaOrbState.LISTENING)
        }
    }

    private fun scheduleReconnect() {
        synchronized(reconnectLock) {
            if (reconnectJob?.isActive == true) return
            reconnectJob = scope.launch {
                mutex.withLock {
                    if (!_isActive.value) return@withLock
                    reconnectAttempts++
                    if (reconnectAttempts > MAX_RECONNECTS) {
                        _lastError.value = "Connection lost - tap Stop and reopen Voice Mode"
                        setState(NovaOrbState.ERROR)
                        return@withLock
                    }
                    endPlayback()
                    setState(NovaOrbState.CONNECTING)
                    delay(RECONNECT_DELAY_MS)
                    if (!_isActive.value) return@withLock
                    latency.reconnected()
                    connectSession(isResuming = true)
                }
            }
        }
    }

    /** Live cannot change the system instruction mid-session, so reconnect with a recap. */
    fun refreshInstructions(ctx: Context) {
        if (!_isActive.value) return
        appContext = ctx.applicationContext
        scope.launch { mutex.withLock { if (_isActive.value) connectSession(isResuming = true) } }
    }

    fun toggleMute() { _muted.value = !_muted.value }

    fun setVolume(v: Float) { audio?.setVolume(v) }

    fun stop() {
        synchronized(reconnectLock) { reconnectJob?.cancel(); reconnectJob = null }
        silenceJob?.cancel()
        epoch.incrementAndGet()
        _isActive.value = false
        client?.disconnect(); client = null
        audio?.release(); audio = null
        micStarted = false
        playing = false
        EchoGuard.reset()
        synchronized(userBuf) { userBuf.setLength(0); modelBuf.setLength(0) }
        ConversationMemory.clear()
        _amplitude.value = 0f
        _vadActive.value = false
        _muted.value = false
        _lastError.value = null
        setState(NovaOrbState.IDLE)
    }
}
