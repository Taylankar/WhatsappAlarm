package com.example.whatsappalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var stopButton: Button

    private val alarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.whatsappalarm.ALARM_TRIGGERED") {
                statusTextView.text = "ALARM ÇALIYOR!\nAlanya ve Gazipaşa yakalandı!"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kod seviyesinde çok basit bir arayüz tasarımı
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            padding = 50
        }

        statusTextView = TextView(this).apply {
            text = "Sistem Beklemede...\nBildirim iznini vermeyi unutmayın."
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 50)
        }

        val permissionButton = Button(this).apply {
            text = "Bildirim Erişimini Aç"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        stopButton = Button(this).apply {
            text = "Alarmı Durdur"
            setOnClickListener {
                val stopIntent = Intent(this@MainActivity, NotificationService::class.java).apply {
                    action = "STOP_ALARM"
                }
                startService(stopIntent)
                statusTextView.text = "Alarm Susturuldu.\nSistem yeniden dinlemede."
            }
        }

        layout.addView(statusTextView)
        layout.addView(permissionButton)
        layout.addView(stopButton)
        setContentView(layout)

        registerReceiver(alarmReceiver, IntentFilter("com.example.whatsappalarm.ALARM_TRIGGERED"), RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(alarmReceiver)
    }
}
