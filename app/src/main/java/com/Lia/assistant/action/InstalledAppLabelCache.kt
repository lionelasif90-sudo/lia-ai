package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Process-wide map: lowercase launcher label -> package. Built once on IO, 15 min TTL. */
object InstalledAppLabelCache {
    private const val TTL_MS = 15 * 60 * 1000L
    private val mutex = Mutex()
    @Volatile private var map: Map<String, String> = emptyMap()
    @Volatile private var builtAt = 0L
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun prewarm(ctx: Context) {
        val app = ctx.applicationContext
        scope.launch { get(app) }
    }

    suspend fun get(ctx: Context): Map<String, String> {
        if (map.isNotEmpty() && System.currentTimeMillis() - builtAt < TTL_MS) return map
        return mutex.withLock {
            if (map.isNotEmpty() && System.currentTimeMillis() - builtAt < TTL_MS) return@withLock map
            val built = withContext(Dispatchers.IO) { build(ctx.applicationContext) }
            map = built
            builtAt = System.currentTimeMillis()
            built
        }
    }

    private fun build(ctx: Context): Map<String, String> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val out = LinkedHashMap<String, String>()
        try {
            for (ri in pm.queryIntentActivities(intent, 0)) {
                val label = ri.loadLabel(pm)?.toString()?.trim()?.lowercase().orEmpty()
                if (label.isNotEmpty() && label !in out) out[label] = ri.activityInfo.packageName
            }
        } catch (_: Exception) {}
        return out
    }
}
