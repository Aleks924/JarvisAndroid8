package com.jarvis.assistant

import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.app.*
import android.content.*
import android.os.*
import android.speech.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import java.util.Locale

class JarvisService : Service(), TextToSpeech.OnInitListener {
    private var porcupine: PorcupineManager? = null
    private var recognizer: SpeechRecognizer? = null
    private lateinit var tts: TextToSpeech
    private val main = Handler(Looper.getMainLooper())
    private var destroyed = false

    override fun onCreate() {
        super.onCreate(); createChannel()
        val n = NotificationCompat.Builder(this, "jarvis").setContentTitle("JARVIS 8 ativo")
            .setContentText("Diga “Jarvis” para chamar.").setSmallIcon(android.R.drawable.ic_btn_speak_now).setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(8, n, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE) else startForeground(8, n)
        tts = TextToSpeech(this, this)
        val key = getSharedPreferences("jarvis", MODE_PRIVATE).getString("pv_key", "").orEmpty()
        if (key.isBlank()) { speak("Abra o JARVIS e coloque a AccessKey do Picovoice."); return }
        try {
            val cb = PorcupineManagerCallback { _ -> onWakeWord() }
            porcupine = PorcupineManager.Builder().setAccessKey(key).setKeyword(Porcupine.BuiltInKeyword.JARVIS)
                .build(applicationContext, cb)
            startWakeWord()
        } catch (_: Exception) { speak("Não consegui iniciar o detector Jarvis. Verifique sua AccessKey.") }
    }

    private fun onWakeWord() { stopWakeWord(); speak("Sim, estou ouvindo.") { captureCommand() } }
    private fun startWakeWord() { if (!destroyed) try { porcupine?.start() } catch (_: Exception) {} }
    private fun stopWakeWord() { try { porcupine?.stop() } catch (_: Exception) {} }

    private fun captureCommand() {
        if (destroyed || !SpeechRecognizer.isRecognitionAvailable(this)) { restartWakeWord(500); return }
        recognizer?.destroy(); recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(b: Bundle?) {}
            override fun onEvent(t: Int, b: Bundle?) {}
            override fun onError(e: Int) { cleanupRecognizer(); restartWakeWord(700) }
            override fun onResults(b: Bundle?) {
                val text = b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                cleanupRecognizer()
                if (text.isBlank()) { restartWakeWord(700); return }
                val handled = CommandRouter.handle(this@JarvisService, text) { reply -> speak(reply) }
                if (!handled) speak("Ainda não tenho esse comando local.") { restartWakeWord(300) }
            }
        })
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR"); putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try { recognizer?.startListening(i) } catch (_: Exception) { cleanupRecognizer(); restartWakeWord(700) }
    }

    private fun cleanupRecognizer() { try { recognizer?.stopListening() } catch (_: Exception) {}; recognizer?.destroy(); recognizer = null }
    private fun restartWakeWord(delay: Long) { main.postDelayed({ startWakeWord() }, delay) }
    private fun createChannel() { if (Build.VERSION.SDK_INT >= 26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("jarvis", "JARVIS", NotificationManager.IMPORTANCE_LOW)) }

    private fun speak(text: String, after: (() -> Unit)? = null) {
        if (!::tts.isInitialized) { after?.invoke(); return }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) { main.post { after?.invoke() } }
            override fun onError(id: String?) { main.post { after?.invoke() } }
        })
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis-${System.nanoTime()}")
    }
    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale("pt", "BR") }
    override fun onBind(i: Intent?) = null
    override fun onDestroy() { destroyed = true; main.removeCallbacksAndMessages(null); stopWakeWord(); try { porcupine?.delete() } catch (_: Exception) {}; cleanupRecognizer(); if (::tts.isInitialized) tts.shutdown(); super.onDestroy() }
}
