package com.Lia.assistant.action

import org.json.JSONArray

object LiaToolCatalog {
    fun declarations(): JSONArray = JSONArray()
        .put(CommonActions.OPEN_APP)
        .put(CommonActions.CALL_CONTACT)
        .put(CommonActions.MESSAGE_CONTACT)
}
