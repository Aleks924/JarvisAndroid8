package com.jarvis.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import java.text.SimpleDateFormat
import java.util.*

object CommandRouter {
    fun handle(c: Context, text: String, reply: (String) -> Unit): Boolean {
        val t = text.lowercase(Locale("pt", "BR")).trim()
        if (t.contains("que horas") || t == "hora") { reply("Agora são " + SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date())); return true }
        fun app(pkg: String, name: String): Boolean { val i = c.packageManager.getLaunchIntentForPackage(pkg); if (i != null) { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); c.startActivity(i); reply("Abrindo $name.") } else reply("$name não está instalado."); return true }
        if (t.contains("abrir whatsapp")) return app("com.whatsapp", "WhatsApp")
        if (t.contains("abrir instagram")) return app("com.instagram.android", "Instagram")
        if (t.contains("abrir youtube")) return app("com.google.android.youtube", "YouTube")
        if (t.contains("abrir configurações")) { c.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); reply("Abrindo as configurações."); return true }
        if (t.startsWith("pesquisar ") || t.startsWith("pesquise ") || t.startsWith("procure ")) { val q=t.replaceFirst(Regex("(?i)^(pesquisar|pesquise|procure)\\s+"),""); c.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+Uri.encode(q))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); reply("Abrindo a pesquisa."); return true }
        if (t.contains("alarme")) { c.startActivity(Intent(AlarmClock.ACTION_SET_ALARM).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); reply("Abrindo o alarme."); return true }
        if (t.startsWith("ligar para ")) { val n=t.removePrefix("ligar para ").trim(); c.startActivity(Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+Uri.encode(n))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); reply("Abrindo o telefone para você confirmar a ligação."); return true }
        return false
    }
}
