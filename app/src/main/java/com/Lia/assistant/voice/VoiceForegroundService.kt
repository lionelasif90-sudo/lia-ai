package com.Lia.assistant.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.Lia.assistant.MainActivity
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.data.NovaPreferences

class VoiceForegroundService : Service() {
    companion object {
        const val ACTION_STOP = "com.Lia.assistant.action.STOP_VOICE"
        private const val NOTIF_ID = 4101
        private const val CHANNEL_ID = "lia_voice_session"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            VoiceSessionManager.stop()
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        val name = NovaPreferences.getString(this, NovaPreferences.Keys.ASSISTANT_NAME, AssistantBrand.NAME)
            .ifBlank { AssistantBrand.NAME }
        ServiceCompat.startForeground(
            this, NOTIF_ID, buildNotification(this, name),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
        )
        VoiceSessionManager.start(this)
        return START_STICKY
    }

    override fun onDestroy() {
        VoiceSessionManager.stop()
        super.onDestroy()
    }

    private fun buildNotification(ctx: Context, name: String): Notification {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "$name voice session", NotificationManager.IMPORTANCE_LOW)
        )
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val stop = PendingIntent.getService(
            ctx, 1, Intent(ctx, VoiceForegroundService::class.java).setAction(ACTION_STOP), flags)
        val open = PendingIntent.getActivity(ctx, 2, Intent(ctx, MainActivity::class.java), flags)
        return NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("$name is listening")
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(0, "Stop", stop)
            .build()
    }
}
