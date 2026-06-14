package com.skytownstudios.cardbornheroes.audio

import android.content.Context
import android.media.MediaPlayer

object SoundManager {
    private var player: MediaPlayer? = null

    fun play(context: Context, name: String) {
        try {
            player?.release()
            val afd = context.assets.openFd("audio/$name.m4a")
            player = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                start()
            }
        } catch (_: Exception) {
            try {
                val afd = context.assets.openFd("audio/$name.mp3")
                player = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    prepare()
                    start()
                }
            } catch (_: Exception) {}
        }
    }
}
