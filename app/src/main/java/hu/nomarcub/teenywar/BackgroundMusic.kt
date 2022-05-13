package hu.nomarcub.teenywar

import android.content.Context
import android.media.MediaPlayer

//TODO Backgroundmusic volume change in onPause/resume
class BackgroundMusic(private val resourceID: Int, private val context: Context, private val maxVolume: Int = 100) {
    private var mediaPlayer: MediaPlayer? = null
    private var isReleased = true
    private var currentPosition = 0

    public fun adjustVolume(volume: Int) {
        if (volume <= 0) {
            currentPosition = mediaPlayer?.currentPosition ?: 0
            mediaPlayer?.reset()

            isReleased = true

        } else {
            if (mediaPlayer == null || isReleased) {
                mediaPlayer = MediaPlayer.create(context, resourceID)
                mediaPlayer?.isLooping = true
                mediaPlayer?.seekTo(currentPosition)
                isReleased = false
            }
            if (mediaPlayer?.isPlaying == false)
                mediaPlayer?.start()

            val actualVolume = Math.pow(volume / maxVolume.toDouble(), 1.8).toFloat()
            //(Math.log(volume + 1.toDouble()) / Math.log(maxVolume + 1.toDouble())).toFloat()
            mediaPlayer?.setVolume(actualVolume, actualVolume)
        }
    }

}