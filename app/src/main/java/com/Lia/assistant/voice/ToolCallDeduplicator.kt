package com.Lia.assistant.voice

class ToolCallDeduplicator(private val maxSize: Int = 256) {
    private val seen = LinkedHashSet<String>()

    /** @return true the first time an id is seen, false for repeats. */
    @Synchronized
    fun firstTime(id: String): Boolean {
        if (!seen.add(id)) return false
        while (seen.size > maxSize) {
            val it = seen.iterator(); it.next(); it.remove()
        }
        return true
    }
}
