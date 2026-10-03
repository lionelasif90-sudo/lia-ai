package com.Lia.assistant.action

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import org.json.JSONObject

/** Flavor-neutral tool bodies. The user always taps Send themselves in the play flavor. */
object CommonActions {
    suspend fun openApp(ctx: Context, args: JSONObject): JSONObject {
        val query = args.optString("app_name")
        val match = AppLauncher.findApp(ctx, query) ?: return JSONObject().put("result", "app_not_found")
        return if (AppLauncher.launch(ctx, match.second))
            JSONObject().put("result", "opened").put("app", match.first)
        else JSONObject().put("result", "launch_failed")
    }

    suspend fun callContact(ctx: Context, args: JSONObject): JSONObject {
        return when (val r = ContactResolver.lookup(ctx, args.optString("contact"))) {
            is ContactResult.NoPermission -> JSONObject().put("result", "contacts_permission_missing")
            is ContactResult.NotFound -> JSONObject().put("result", "contact_not_found")
            is ContactResult.Found -> {
                val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                val action = if (granted) Intent.ACTION_CALL else Intent.ACTION_DIAL
                val intent = Intent(action, Uri.parse("tel:${Uri.encode(r.number)}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    ctx.startActivity(intent)
                    JSONObject().put("result", if (granted) "calling" else "dialer_opened_no_permission").put("contact", r.name)
                } catch (_: Exception) { JSONObject().put("result", "failed") }
            }
        }
    }

    /** Opens the composer only. Never SmsManager. @return pair(resultJson, number-if-opened) */
    suspend fun openMessageComposer(ctx: Context, args: JSONObject): Pair<JSONObject, String?> {
        val message = args.optString("message")
        val viaWhatsApp = args.optString("app").lowercase().contains("whatsapp")
        return when (val r = ContactResolver.lookup(ctx, args.optString("contact"))) {
            is ContactResult.NoPermission -> JSONObject().put("result", "contacts_permission_missing") to null
            is ContactResult.NotFound -> JSONObject().put("result", "contact_not_found") to null
            is ContactResult.Found -> {
                val intent = if (viaWhatsApp) CommIntents.whatsappIntent(ctx, r.number, message)
                else CommIntents.smsIntent(r.number, message)
                try {
                    ctx.startActivity(intent)
                    JSONObject().put("result", "composer_opened").put("contact", r.name) to (if (viaWhatsApp) "whatsapp" else "sms")
                } catch (_: Exception) { JSONObject().put("result", "failed") to null }
            }
        }
    }

    val OPEN_APP = functionDecl("open_app", "Open an installed app by its name.",
        mapOf("app_name" to "Name of the app, e.g. YouTube"), listOf("app_name"))
    val CALL_CONTACT = functionDecl("call_contact", "Call a contact by name or phone number.",
        mapOf("contact" to "Contact name or phone number"), listOf("contact"))
    val MESSAGE_CONTACT = functionDecl("message_contact",
        "Prepare a message to a contact. The user reviews it and taps Send.",
        mapOf("contact" to "Contact name or phone number", "message" to "Message text", "app" to "sms or whatsapp"),
        listOf("contact", "message"))
}
