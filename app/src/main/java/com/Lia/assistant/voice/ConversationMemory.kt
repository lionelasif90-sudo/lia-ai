package com.Lia.assistant.voice

enum class Role { USER, ASSISTANT }
data class Turn(val role: Role, val text: String)

/** In-memory rolling window; cleared when the session stops. */
object ConversationMemory {
    private const val MAX_TURNS = 12
    private const val MAX_CHARS = 1600
    private val turns = ArrayDeque<Turn>()

    fun add(role: Role, text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        synchronized(turns) {
            turns.addLast(Turn(role, t))
            while (turns.size > MAX_TURNS) turns.removeFirst()
        }
    }

    fun clear() = synchronized(turns) { turns.clear() }

    fun isEmpty(): Boolean = synchronized(turns) { turns.isEmpty() }

    fun recap(): String {
        val text = synchronized(turns) {
            turns.joinToString("\n") { (if (it.role == Role.USER) "User: " else "You: ") + it.text }
        }
        return if (text.length <= MAX_CHARS) text else text.takeLast(MAX_CHARS)
    }
}
