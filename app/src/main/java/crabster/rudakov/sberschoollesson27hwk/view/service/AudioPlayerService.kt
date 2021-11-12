package crabster.rudakov.sberschoollesson27hwk.view.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import crabster.rudakov.sberschoollesson27hwk.R
import crabster.rudakov.sberschoollesson27hwk.utils.Constants
import crabster.rudakov.sberschoollesson27hwk.view.IPlaybackListener
import crabster.rudakov.sberschoollesson27hwk.view.MediaPlayerWrapper

/**
 * Foreground Service. Воспроизводит ранее сделанные записи
 */
class AudioPlayerService : Service() {

    private val audioPlayerServiceBinder = LocalAudioPlayerServiceBinder()
    private val mediaPlayerWrapper = MediaPlayerWrapper()
    private var playerServiceListener: IOnPlayerServiceListener? = null

    /**
     * Создание сервиса
     */
    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        setProgressListener()
    }

    /**
     * Привязать сервис
     */
    override fun onBind(intent: Intent?): IBinder {
        return audioPlayerServiceBinder
    }

    /**
     * Начать воспроизведение
     */
    fun startPlaying(pathFile: String) {
        mediaPlayerWrapper.startPlaying(pathFile)
    }

    /**
     * Остановить воспроизведение
     */
    fun stopPlaying() {
        mediaPlayerWrapper.stopPlaying()
    }

    /**
     * Создание нотификации
     */
    private fun createNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
//            .setContentTitle(R.string.app_name.toString())
        return notificationBuilder.build()
    }

    /**
     * Запустить сервис
     */
    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(Constants.NOTIFICATION_PLAYER_ID, createNotification())
    }

    /**
     * Создать канал нотификации
     *
     * @param notificationManager менеджер нотификаций
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_PLAYER_CHANNEL_ID,
            Constants.NOTIFICATION_PLAYER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Устанавливает слушателя прогресса воспроизведения из сервиса
     */
    private fun setProgressListener() {
        mediaPlayerWrapper.setOnProgressListener(object : IPlaybackListener {

            /**
             * Устанавливает слушателя прогресса воспроизведения из сервиса
             *
             * @param duration длительность записи в миллисекундах
             */
            override fun onPlayStarted(duration: Int) {
                playerServiceListener?.onPlayStarted(duration)
            }

            /**
             * Получает позицию прогресса воспроизведения через интерфейс
             *
             * @param currentPos текущая позиция в миллисекундах
             */
            override fun onProgressChanged(currentPos: Int, duration: Int) {
                playerServiceListener?.onPlayProgressChanged(currentPos)
            }

            /**
             * Останавливает воспроизведение
             */
            override fun onPlayStopped() {
                playerServiceListener?.onPlayStopped()
            }
        })
    }

    /**
     * Устанавливает слушателя [IOnPlayerServiceListener] из Activity
     *
     * @param listener инстанс слушателя
     */
    fun setPlayerServiceListener(listener: IOnPlayerServiceListener) {
        playerServiceListener = listener
    }

    /**
     * Класс [LocalAudioRecorderServiceBinder] возвращает обьект [AudioPlayerService]
     */
    inner class LocalAudioPlayerServiceBinder : Binder() {

        fun getAudioPlayerService(): AudioPlayerService {
            return this@AudioPlayerService
        }

    }

    /**
     * Интерфейс слушателя изменений статуса и прогресса воспроизведения записей
     */
    interface IOnPlayerServiceListener {

        /**
         * Вызывается при начале воспроизведения
         *
         * @param duration длительность записи в миллисекундах
         */
        fun onPlayStarted(duration: Int)

        /**
         * Вызывается при изменении прогресса воспроизведения
         *
         * @param currentPosition текущая позиция в миллисекундах
         */
        fun onPlayProgressChanged(currentPosition: Int)

        /**
         * Вызывается при окончании воспроизведения
         */
        fun onPlayStopped()
    }

}