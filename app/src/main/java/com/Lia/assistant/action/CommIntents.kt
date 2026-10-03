package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

object CommIntents {
    fun digits(number: String): String = number.filter { it.isDigit() }

    fun smsIntent(number: String, body: String): Intent =
        Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(number.trim())}"))
            .putExtra("sms_body", body)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun whatsappUrl(number: String, text: String): String =
        "https://wa.me/${digits(number)}?text=${URLEncoder.encode(text, "UTF-8")}"

    fun whatsappPackage(ctx: Context): String? =
        listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { pkg ->
            try { ctx.packageManager.getPackageInfo(pkg, 0); true } catch (_: Exception) { false }
        }

    fun whatsappIntent(ctx: Context, number: String, text: String): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl(number, text))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        whatsappPackage(ctx)?.let { i.setPackage(it) }
        return i
    }
}
