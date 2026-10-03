package com.Lia.assistant.action

import android.content.Context
import org.json.JSONObject

object ActionExecutor {
    suspend fun execute(ctx: Context, name: String, args: JSONObject): JSONObject = when (name) {
        "open_app" -> CommonActions.openApp(ctx, args)
        "call_contact" -> CommonActions.callContact(ctx, args)
        "message_contact" -> CommonActions.openMessageComposer(ctx, args).first // user taps Send
        else -> JSONObject().put("error", "Unknown tool: $name")
    }
}
