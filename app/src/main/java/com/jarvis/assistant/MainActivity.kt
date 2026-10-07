package com.jarvis.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b); setContentView(R.layout.activity_main)
        val status: TextView = findViewById(R.id.status)
        val key: EditText = findViewById(R.id.apiKey)
        val pvKey: EditText = findViewById(R.id.pvKey)
        val prefs = getSharedPreferences("jarvis", MODE_PRIVATE)
        key.setText(prefs.getString("api_key",""))
        pvKey.setText(prefs.getString("pv_key",""))

        findViewById<Button>(R.id.start).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10); return@setOnClickListener
            }
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 11)
            }
            prefs.edit().putString("api_key", key.text.toString().trim())
                .putString("pv_key", pvKey.text.toString().trim()).apply()
            ContextCompat.startForegroundService(this, Intent(this, JarvisService::class.java))
            status.text = "Ouvindo a palavra Jarvis"
        }
        findViewById<Button>(R.id.stop).setOnClickListener {
            stopService(Intent(this, JarvisService::class.java)); status.text = "Desativado"
        }
    }
}
