package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundManager(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {}
    }

    fun playDialpadTone(digit: Char) {
        try {
            val tone = when (digit) {
                '1' -> ToneGenerator.TONE_DTMF_1
                '2' -> ToneGenerator.TONE_DTMF_2
                '3' -> ToneGenerator.TONE_DTMF_3
                '4' -> ToneGenerator.TONE_DTMF_4
                '5' -> ToneGenerator.TONE_DTMF_5
                '6' -> ToneGenerator.TONE_DTMF_6
                '7' -> ToneGenerator.TONE_DTMF_7
                '8' -> ToneGenerator.TONE_DTMF_8
                '9' -> ToneGenerator.TONE_DTMF_9
                '0' -> ToneGenerator.TONE_DTMF_0
                '*' -> ToneGenerator.TONE_DTMF_S
                '#' -> ToneGenerator.TONE_DTMF_P
                else -> ToneGenerator.TONE_PROP_BEEP
            }
            toneGenerator?.startTone(tone, 120)
        } catch (_: Exception) {}
    }

    fun playMessageReceivedChime() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                delay(90)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
            } catch (_: Exception) {}
        }
    }

    fun playSPenSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 30)
        } catch (_: Exception) {}
    }

    fun playCallRingTone() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repeat(4) {
                    toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 800)
                    delay(1400)
                }
            } catch (_: Exception) {}
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
