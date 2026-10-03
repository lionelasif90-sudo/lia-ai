package com.Lia.assistant.action

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ContactResult {
    data class Found(val name: String, val number: String) : ContactResult()
    object NoPermission : ContactResult()
    object NotFound : ContactResult()
}

object ContactResolver {
    fun looksLikePhone(q: String): Boolean {
        val t = q.trim()
        return t.isNotEmpty() && t.all { it.isDigit() || it in " +-()." } && t.count { it.isDigit() } >= 5
    }

    /** exact (0) > prefix (1) > contains (2). */
    fun rank(candidates: List<Pair<String, String>>, query: String): Pair<String, String>? {
        val q = query.trim().lowercase()
        return candidates.minByOrNull { (name, _) ->
            val n = name.lowercase()
            when {
                n == q -> 0
                n.startsWith(q) -> 1
                n.contains(q) -> 2
                else -> 3
            }
        }
    }

    suspend fun lookup(ctx: Context, query: String): ContactResult {
        if (looksLikePhone(query)) return ContactResult.Found(query.trim(), query.trim())
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return ContactResult.NoPermission
        }
        val found = withContext(Dispatchers.IO) {
            val list = mutableListOf<Pair<String, String>>()
            try {
                ctx.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ),
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                    arrayOf("%${query.trim()}%"),
                    null,
                )?.use { c ->
                    while (c.moveToNext() && list.size < 5) {
                        list += (c.getString(0).orEmpty() to c.getString(1).orEmpty())
                    }
                }
            } catch (_: SecurityException) {}
            list
        }
        val best = rank(found, query) ?: return ContactResult.NotFound
        return ContactResult.Found(best.first, best.second)
    }
}
