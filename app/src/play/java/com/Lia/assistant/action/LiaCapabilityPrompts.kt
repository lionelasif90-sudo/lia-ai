package com.Lia.assistant.action

object LiaCapabilityPrompts {
    fun toolInstruction(name: String): String =
        "Phone abilities: you can open apps, call contacts and prepare messages (open_app, call_contact, message_contact). " +
            "For messages you only open the composer; the user taps Send themselves, so say that. " +
            "You cannot read or control the screen, and you cannot post to social apps; if asked, say so plainly. " +
            "Call a tool only when the user asks for an action."
}
