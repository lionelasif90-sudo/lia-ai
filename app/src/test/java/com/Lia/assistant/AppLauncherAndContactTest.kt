package com.Lia.assistant

import com.Lia.assistant.action.AppLauncher
import com.Lia.assistant.action.ContactResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLauncherAndContactTest {
    private val labels = mapOf(
        "youtube" to "com.google.android.youtube",
        "youtube music" to "com.google.android.apps.youtube.music",
        "whatsapp" to "com.whatsapp",
        "camera" to "com.cam",
        "ca" to "com.tiny",
    )

    @Test fun exactWins() = assertEquals("com.google.android.youtube", AppLauncher.matchLabel("YouTube", labels)?.second)
    @Test fun prefixMatches() = assertEquals("com.whatsapp", AppLauncher.matchLabel("whats", labels)?.second)
    @Test fun queryStartsWithKey() = assertEquals("com.whatsapp", AppLauncher.matchLabel("whatsapp business", labels)?.second)
    @Test fun noLooseSubstring() = assertNull(AppLauncher.matchLabel("tube", labels))
    @Test fun shortKeysIgnored() = assertNull(AppLauncher.matchLabel("c", mapOf("ca" to "x")))
    @Test fun blankIsNull() = assertNull(AppLauncher.matchLabel("  ", labels))

    @Test fun phoneDetection() {
        assertTrue(ContactResolver.looksLikePhone("+91 98765-43210"))
        assertFalse(ContactResolver.looksLikePhone("Rahul"))
        assertFalse(ContactResolver.looksLikePhone("1234"))
    }

    @Test fun rankExactPrefixContains() {
        val c = listOf("Anand Rahul" to "3", "Rahul Sharma" to "2", "Rahul" to "1")
        assertEquals("1", ContactResolver.rank(c, "rahul")?.second)
        assertEquals("2", ContactResolver.rank(listOf("Anand Rahul" to "3", "Rahul Sharma" to "2"), "rahul")?.second)
    }
}
