package com.example.service

import android.content.Context
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import com.example.model.NarrationCue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Handles speech synthesis and Bengali voice-over preview
 * Configures female pitch, speed, and Bengali (India/West Bengal) locale
 */
class BengaliAudioVoiceEngine(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try India Bengali ("ben", "IN") for West Bengal accent
                val bengaliLocale = Locale("bn", "IN")
                val result = textToSpeech?.setLanguage(bengaliLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to general Bengali
                    textToSpeech?.setLanguage(Locale("bn"))
                }
                // Tune to warm pleasant female pitch
                textToSpeech?.setPitch(1.15f)
                textToSpeech?.setSpeechRate(0.95f)

                // Select female voice if available among system voices
                try {
                    val voices = textToSpeech?.voices
                    val femaleVoice = voices?.firstOrNull { voice ->
                        voice.locale.language == "bn" && voice.name.lowercase().contains("female")
                    } ?: voices?.firstOrNull { it.locale.language == "bn" }
                    if (femaleVoice != null) {
                        textToSpeech?.voice = femaleVoice
                    }
                } catch (e: Exception) {
                    Log.w("BengaliAudioEngine", "Voice selection fallback", e)
                }

                isTtsReady = true
            }
        }
    }

    fun speakNarration(textBn: String, onStart: () -> Unit = {}, onDone: () -> Unit = {}) {
        if (textBn.isBlank()) return
        if (textToSpeech != null) {
            textToSpeech?.speak(textBn, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_${System.currentTimeMillis()}")
            onStart()
        }
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
