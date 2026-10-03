package com.Lia.assistant.action

import android.content.Context
import kotlinx.coroutines.delay
import org.json.JSONObject

object ActionExecutor {
    private const val NOT_ENABLED = "accessibility_not_enabled"

    suspend fun execute(ctx: Context, name: String, args: JSONObject): JSONObject {
        val svc = NovaAccessibilityService.instance
        return when (name) {
            "open_app" -> CommonActions.openApp(ctx, args)
            "call_contact" -> CommonActions.callContact(ctx, args)
            "message_contact" -> messageContact(ctx, args)
            "read_screen" -> if (svc == null) res(NOT_ENABLED) else JSONObject().put("result", "ok").put("screen", svc.readScreen())
            "tap_text" -> if (svc == null) res(NOT_ENABLED) else res(if (svc.tapText(args.optString("text"))) "tapped" else "element_not_found")
            "type_text" -> if (svc == null) res(NOT_ENABLED) else res(if (svc.typeText(args.optString("text"))) "typed" else "no_focused_field")
            "scroll_screen" -> if (svc == null) res(NOT_ENABLED)
                else res(if (svc.scroll(args.optString("direction").lowercase() != "up")) "scrolled" else "nothing_scrollable")
            "go_back" -> if (svc == null) res(NOT_ENABLED) else res(if (svc.back()) "done" else "failed")
            "go_home" -> if (svc == null) res(NOT_ENABLED) else res(if (svc.home()) "done" else "failed")
            else -> JSONObject().put("error", "Unknown tool: $name")
        }
    }

    private fun res(s: String) = JSONObject().put("result", s)

    /** Opens the composer, then (if accessibility is on) taps Send: up to 12 tries, 400 ms apart. */
    private suspend fun messageContact(ctx: Context, args: JSONObject): JSONObject {
        val (opened, kind) = CommonActions.openMessageComposer(ctx, args)
        if (kind == null) return opened
        val svc = NovaAccessibilityService.instance ?: return opened
        repeat(12) {
            delay(400)
            if (kind == "whatsapp") svc.tapText("Continue to chat")
            if (svc.tapText("Send")) return res("sent").put("contact", opened.optString("contact"))
        }
        return res("composer_opened_send_not_found")
    }
}
