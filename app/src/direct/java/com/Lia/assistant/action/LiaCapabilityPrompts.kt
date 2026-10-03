package com.Lia.assistant.action

object LiaCapabilityPrompts {
    fun toolInstruction(name: String): String =
        "Phone abilities (tools): open_app, call_contact, message_contact, read_screen, tap_text, type_text, " +
            "scroll_screen, go_back, go_home. Call a tool only when the user asks for an action. " +
            "Use read_screen before tapping when you are unsure what is on screen, then describe results briefly. " +
            "If a tool says accessibility_not_enabled, tell the user to enable the Accessibility service in Settings. " +
            "Never do anything irreversible (sending, posting, paying, deleting) without the user's explicit yes."
}
