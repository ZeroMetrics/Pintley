package com.example.pintly

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Tiny shared sound player. A light "pop" for taps/buttons and a grander sound
 * reserved for the Ultra Challenge reveal. Loaded once and reused across screens.
 */
object SoundManager {

    private var pool: SoundPool? = null
    private var popId = 0
    private var ultraId = 0

    fun init(context: Context) {
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val sp = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()
        popId = sp.load(context.applicationContext, R.raw.pop, 1)
        ultraId = sp.load(context.applicationContext, R.raw.ultra, 1)
        pool = sp
    }

    /** Light but audible click for buttons and advancing a card. */
    fun playPop() {
        pool?.play(popId, 0.8f, 0.8f, 0, 0, 1f)
    }

    /** Grander sound for the rare Ultra Challenge. */
    fun playUltra() {
        pool?.play(ultraId, 1f, 1f, 1, 0, 1f)
    }
}
