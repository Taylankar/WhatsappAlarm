package com.example.whatsappalarm

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.Locale

class NotificationService : NotificationListenerService() {

    private var ringtone: Ringtone? = null
    private var lastTriggerTime: Long = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_ALARM") {
            stopAlarm()
        }
        return START_STICKY
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        // Sadece WhatsApp Business bildirimlerini filtrele
        if (sbn.packageName == "com.whatsapp.w4b") {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title")?.lowercase(Locale.ROOT) ?: ""
            val text = extras.getCharSequence("android.text")?.toString()?.lowercase(Locale.ROOT) ?: ""
            
            val fullMessage = "$title $text"

            // Kelime kontrolleri (Alanya VE Gazipaşa)
            val containsAlanya = fullMessage.contains("alanya")
            // Türkçe karakter uyumluluğu için hem 'ş' hem 's' kontrolü yapıyoruz
            val containsGazipasa = fullMessage.contains("gazipaşa") || fullMessage.contains("gazipasa")

            if (containsAlanya && containsGazipasa) {
                val currentTime = System.currentTimeMillis()
                // 10 saniye içinde peş peşe çalmayı engelleme filtresi
                if (currentTime - lastTriggerTime > 10000) {
                    lastTriggerTime = currentTime
                    triggerAlarm()
                }
            }
        }
    }

    private fun triggerAlarm() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Telefonun medya ve alarm ses seviyelerini %100 yap
            val maxMediaVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMediaVolume, 0)
            
            val maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVolume, 0)

            // Varsayılan alarm sesini yükle ve çal
            if (ringtone == null || !ringtone!!.isPlaying) {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                
                ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone?.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone?.play()

                // Arayüze alarmın çaldığı bilgisini gönder
                val intent = Intent("com.example.whatsappalarm.ALARM_TRIGGERED")
                sendBroadcast(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarm() {
        if (ringtone != null && ringtone!!.isPlaying) {
            ringtone?.stop()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}
}
