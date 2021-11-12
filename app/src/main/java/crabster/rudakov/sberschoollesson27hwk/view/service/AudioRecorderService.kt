package crabster.rudakov.sberschoollesson27hwk.view.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.View.*
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import crabster.rudakov.sberschoollesson27hwk.R
import crabster.rudakov.sberschoollesson27hwk.utils.Constants
import crabster.rudakov.sberschoollesson27hwk.utils.Constants.ACTION_START_RECORDING
import crabster.rudakov.sberschoollesson27hwk.utils.Constants.ACTION_STOP_RECORDING
import crabster.rudakov.sberschoollesson27hwk.utils.Constants.NOTIFICATION_CHANNEL_ID
import crabster.rudakov.sberschoollesson27hwk.utils.Constants.NOTIFICATION_ID
import crabster.rudakov.sberschoollesson27hwk.utils.TimeFormatter
import crabster.rudakov.sberschoollesson27hwk.view.MediaRecorderWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Foreground Service. Записывает звук с микрофона устройства.
 * Управляет записью в нотификации
 */
class AudioRecorderService : Service() {

    private val audioRecorderServiceBinder = LocalAudioRecorderServiceBinder()
    private val mediaRecorderWrapper = MediaRecorderWrapper(this)
    private lateinit var timerListener: OnTimerChangedListener
    private var timeStarted = 0L
    private var isTimerEnabled = false
    private var currentTime = 0L

    /**
     * Создание сервиса
     */
    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    /**
     * Привязывание сервиса
     */
    override fun onBind(intent: Intent?): IBinder {
        return audioRecorderServiceBinder
    }

    /**
     * Реакция на команды пользователя
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_STOP_RECORDING -> stopRecording()
        }
        return START_NOT_STICKY
    }

    /**
     * Запускает запись с микрофона
     */
    fun startRecording() {
        mediaRecorderWrapper.startRecording()
        timerListener.onStartRecordingClicked()
        startTimer()
    }

    /**
     * Останавливает запись с микрофона
     */
    fun stopRecording() {
        mediaRecorderWrapper.stopRecording()
        stopTimer()
        timerListener.onStopRecordingClicked()
        stopSelf()
    }

    /**
     * Создание нотификации
     */
    private fun createNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_record_voice_over_24)
            .setContentTitle(R.string.app_name.toString())
            .setContent(getRemoteViews())
        return notificationBuilder.build()
    }

    /**
     * Запуск сервиса
     */
    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID, createNotification())
    }

    /**
     * Создать канал нотификации
     *
     * @param notificationManager менеджер уведомлений
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Установка параметров RemoteViews в зависимости от происходящих в нотификации событий
     */
    private fun getRemoteViews(): RemoteViews {
        val remoteViews = RemoteViews(packageName, R.layout.recording_service_notification)

        val startRecordingIntent = Intent(this, AudioRecorderService::class.java)
        startRecordingIntent.action = ACTION_START_RECORDING
        val startRecordingPendingIntent = PendingIntent.getService(this, 0, startRecordingIntent, 0)

        val stopRecordingIntent = Intent(this, AudioRecorderService::class.java)
        stopRecordingIntent.action = ACTION_STOP_RECORDING
        val stopRecordingPendingIntent = PendingIntent.getService(this, 0, stopRecordingIntent, 0)

        remoteViews.setTextViewText(R.id.notification_timer, TimeFormatter.formatTime(currentTime))

        if (isTimerEnabled) {
            remoteViews.setImageViewResource(
                R.id.notification_record_button,
                R.drawable.ic_baseline_fiber_manual_record_24_gray)
            remoteViews.setOnClickPendingIntent(R.id.notification_record_button, null)
            remoteViews.setImageViewResource(
                R.id.notification_stop_button,
                R.drawable.ic_baseline_stop_24)
            remoteViews.setOnClickPendingIntent(R.id.notification_stop_button, stopRecordingPendingIntent)
            remoteViews.setViewVisibility(R.id.recording_notification, VISIBLE)
        } else {
            remoteViews.setImageViewResource(
                R.id.notification_record_button,
                R.drawable.ic_baseline_fiber_manual_record_24)
            remoteViews.setOnClickPendingIntent(R.id.notification_record_button, startRecordingPendingIntent)
            remoteViews.setImageViewResource(
                R.id.notification_stop_button,
                R.drawable.ic_baseline_stop_24_gray)
            remoteViews.setOnClickPendingIntent(R.id.notification_stop_button, null)
            remoteViews.setViewVisibility(R.id.recording_notification, INVISIBLE)
        }
        return remoteViews
    }

    /**
     * Запуск таймера звукозаписи
     */
    private fun startTimer() {
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTimerEnabled) {
                currentTime = System.currentTimeMillis() - timeStarted
                updateNotification(createNotification())
                timerListener.onTimerChanged(TimeFormatter.formatTime(currentTime))
                delay(1000)
            }
        }
    }

    /**
     * Обновление нотификации
     */
    private fun updateNotification(notification: Notification) {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Остановка таймера звукозаписи
     */
    private fun stopTimer() {
        isTimerEnabled = false
        currentTime = 0L
        timerListener.onTimerChanged(TimeFormatter.formatTime(currentTime))
        updateNotification(createNotification())
    }

    /**
     * Устанавливает слушателя [OnTimerChangedListener] из Activity
     *
     * @param listener инстанс слушателя изменений таймера
     */
    fun setOnTimerChangedListener(listener: OnTimerChangedListener) {
        timerListener = listener
    }

    /**
     * Класс [LocalAudioRecorderServiceBinder] возвращает обьект [AudioRecorderService]
     */
    inner class LocalAudioRecorderServiceBinder : Binder() {

        fun getAudioRecorderService(): AudioRecorderService {
            return this@AudioRecorderService
        }

    }

    /**
     * Интерфейс слушателя изменений значения таймера и нажатий на кнопки в нотификации
     */
    interface OnTimerChangedListener {

        /**
         * Вызывается при изменении времени таймера
         *
         * @param time время которое передается в активити
         */
        fun onTimerChanged(time: String)

        /**
         * Вызывается при нажатии кнопки Stop в нотификации
         */
        fun onStopRecordingClicked()

        /**
         * Вызывается при нажатии кнопки Record в нотификации
         */
        fun onStartRecordingClicked()
    }

}