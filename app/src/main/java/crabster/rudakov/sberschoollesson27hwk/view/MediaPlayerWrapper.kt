package crabster.rudakov.sberschoollesson27hwk.view

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Класс отвечает за воспроизведение ранее записанного файла
 */
class MediaPlayerWrapper : MediaPlayer.OnCompletionListener {

    private var player: MediaPlayer? = null
    private var progressListener: IPlaybackListener? = null

    /**
     * Начать воспроизведение
     *
     * @param fileName путь к файлу
     */
    fun startPlaying(fileName: String) {

        player = MediaPlayer()
        try {
            player?.setDataSource(fileName)
            player?.prepare()
            player?.start()
            player?.setOnCompletionListener(this)
        } catch (e: IOException) {
            Log.e("MediaPlayer", "prepare() failed")
        }
        player?.let {
            if (it.isPlaying) progressListener?.onPlayStarted(player!!.duration)
        }
        updateProgress()
    }

    /**
     * Обновить значение шкалы прогресса воспроизведения записи
     */
    private fun updateProgress() {
        CoroutineScope(Dispatchers.Main).launch {
            while (player != null && player!!.isPlaying) {
                progressListener?.onProgressChanged(player!!.currentPosition, player!!.duration)
                delay(200)
            }
        }
    }

    /**
     * Остановить воспроизведение
     */
    fun stopPlaying() {
        player?.stop()
        progressListener?.onPlayStopped()
        player?.release()
        player = null
    }

    /**
     * Устанавливает слушателя прогресса воспроизведения из сервиса
     */
    fun setOnProgressListener(listener: IPlaybackListener) {
        progressListener = listener
    }

    /**
     * Закончить воспроизведение
     *
     * @param mp медиа-плейер
     */
    override fun onCompletion(mp: MediaPlayer?) {
        stopPlaying()
    }

}