package com.Lia.assistant.voice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Testable core: inject a scope whose clock you control. */
class EchoGuardCore(
    private val scope: CoroutineScope,
    private val tailMs: Long = 450L,
    private val watchdogMs: Long = 60_000L,
) {
    private val lock = Any()
    private var active = 0
    @Volatile private var muted = false
    private var tailJob: Job? = null
    private var watchdogJob: Job? = null

    fun notifyPlaybackStarted() = synchronized(lock) {
        active++
        muted = true
        tailJob?.cancel(); tailJob = null
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            delay(watchdogMs)
            synchronized(lock) { active = 0; muted = false }
        }
    }

    fun notifyPlaybackStopped() = synchronized(lock) {
        if (active > 0) active--
        if (active == 0) {
            tailJob?.cancel()
            tailJob = scope.launch {
                delay(tailMs)
                synchronized(lock) {
                    if (active == 0) { muted = false; watchdogJob?.cancel() }
                }
            }
        }
    }

    fun isMicMuted(): Boolean = muted

    fun reset() = synchronized(lock) {
        active = 0; muted = false
        tailJob?.cancel(); tailJob = null
        watchdogJob?.cancel(); watchdogJob = null
    }
}

/** Loudspeaker guard: Lia must not hear herself. */
object EchoGuard {
    const val ECHO_TAIL_MS = 450L
    private val core = EchoGuardCore(CoroutineScope(SupervisorJob() + Dispatchers.Default), ECHO_TAIL_MS)
    fun notifyPlaybackStarted() = core.notifyPlaybackStarted()
    fun notifyPlaybackStopped() = core.notifyPlaybackStopped()
    fun isMicMuted() = core.isMicMuted()
    fun reset() = core.reset()
}
