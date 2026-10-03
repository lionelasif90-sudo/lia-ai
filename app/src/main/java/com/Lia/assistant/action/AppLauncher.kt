package com.Lia.assistant.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import kotlin.math.abs

object AppLauncher {
    /** Exact label first; then prefix / word-prefix candidates (key >= 3 chars). Never loose substring matching. */
    fun matchLabel(query: String, labels: Map<String, String>): Pair<String, String>? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null
        labels[q]?.let { return q to it }
        val qWords = q.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val candidates = labels.filterKeys { key ->
            key.length >= 3 && (
                key.startsWith(q) || q.startsWith(key) ||
                    qWords.all { qw -> key.split(' ').any { kw -> kw.startsWith(qw) } }
                )
        }
        val best = candidates.minByOrNull { abs(it.key.length - q.length) } ?: return null
        return best.key to best.value
    }

    suspend fun findApp(ctx: Context, query: String): Pair<String, String>? =
        matchLabel(query, InstalledAppLabelCache.get(ctx))

    fun launch(ctx: Context, packageName: String): Boolean {
        return try {
            val intent = ctx.packageManager.getLaunchIntentForPackage(packageName) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) { false } catch (_: SecurityException) { false }
    }
}
