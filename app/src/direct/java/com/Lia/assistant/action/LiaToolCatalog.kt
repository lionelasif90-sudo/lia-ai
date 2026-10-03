package com.Lia.assistant.action

import org.json.JSONArray

object LiaToolCatalog {
    fun declarations(): JSONArray = JSONArray()
        .put(CommonActions.OPEN_APP)
        .put(CommonActions.CALL_CONTACT)
        .put(CommonActions.MESSAGE_CONTACT)
        .put(functionDecl("read_screen", "Read the visible text and buttons on the current screen."))
        .put(functionDecl("tap_text", "Tap the on-screen element showing this text.",
            mapOf("text" to "Visible text or label to tap"), listOf("text")))
        .put(functionDecl("type_text", "Type text into the focused input field.",
            mapOf("text" to "Text to type"), listOf("text")))
        .put(functionDecl("scroll_screen", "Scroll the current screen.",
            mapOf("direction" to "up or down")))
        .put(functionDecl("go_back", "Press the Back button."))
        .put(functionDecl("go_home", "Go to the home screen."))
}
