package crabster.rudakov.sberschoollesson27hwk.view

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import crabster.rudakov.sberschoollesson27hwk.R
import crabster.rudakov.sberschoollesson27hwk.adapter.IRecyclerListener
import crabster.rudakov.sberschoollesson27hwk.adapter.RecordingsRecyclerAdapter
import crabster.rudakov.sberschoollesson27hwk.utils.Constants.REQUEST_RECORD_AUDIO_PERMISSION
import crabster.rudakov.sberschoollesson27hwk.view.service.AudioPlayerService
import crabster.rudakov.sberschoollesson27hwk.view.service.AudioRecorderService
import java.io.File

/**
 * Главное активити приложения.
 * Показывает кнопки управления записью и воспроизведением.
 * Показывает список сохраненных записей
 */
class MainActivity : AppCompatActivity() {

    private var audioRecorderService: AudioRecorderService? = null
    private var audioPlayerService: AudioPlayerService? = null
    private var audioRecorderServiceConnection: AudioRecorderServiceConnection? = null
    private var audioPlayerServiceConnection: AudioPlayerServiceConnection? = null

    private var recordButton: ImageView? = null
    private var timerTextView: TextView? = null
    private var recordingTextView: TextView? = null
    private var progressBar: SeekBar? = null
    private var stopPlayingButton: ImageView? = null
    private var recordingsRecyclerView: RecyclerView? = null
    private var adapter: RecordingsRecyclerAdapter? = null
    private var isRecording = false
    private var permissions: Array<String> =
        arrayOf(Manifest.permission.RECORD_AUDIO)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        audioRecorderServiceConnection = AudioRecorderServiceConnection()
        audioPlayerServiceConnection = AudioPlayerServiceConnection()
        recordButton = findViewById(R.id.recording_fab)
        timerTextView = findViewById(R.id.timer_textView)
        recordingTextView = findViewById(R.id.recording_textView)
        recordingsRecyclerView = findViewById(R.id.recordings_recyclerView)
        progressBar = findViewById(R.id.play_progress)
        stopPlayingButton = findViewById(R.id.stop_playing_imageView)

        bindRecordingService()
        bindPlayerService()
        initRecyclerView()
        updateRecordingsList()

        recordButton?.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecordingService()
            }
        }

        stopPlayingButton?.setOnClickListener { stopPlaying() }
    }

    /**
     * Инициализация RecyclerView с установлением Listener, позволяющего по
     * нажатию прослушивать запись
     */
    private fun initRecyclerView() {
        recordingsRecyclerView?.layoutManager = LinearLayoutManager(this)
        adapter = RecordingsRecyclerAdapter(object : IRecyclerListener {
            override fun onRecordingClick(pathFile: String) {
                audioPlayerService?.startPlaying(pathFile)
            }
        })
        recordingsRecyclerView?.adapter = adapter
    }

    /**
     * Обновление списка записанных файлов
     */
    private fun updateRecordingsList() {
        val dirName: File? = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        dirName?.listFiles()?.asList()?.let { adapter?.setData(it) }
    }

    /**
     * Запуск звукозаписывающего сервиса
     */
    private fun startRecordingService() {
        startRecording()
    }

    /**
     * Обновление звукозаписывающего пользовательского интерфейса
     */
    private fun updateRecordingUi() {
        if (isRecording) {
            recordButton?.setImageLevel(1)
            recordingTextView?.visibility = VISIBLE
        } else {
            recordButton?.setImageLevel(0)
            recordingTextView?.visibility = GONE
        }
    }

    /**
     * Запуск звукозаписи
     */
    private fun startRecording() {
        audioRecorderService?.startRecording()
    }

    /**
     * Остановка звукозаписи
     */
    private fun stopRecording() {
        audioRecorderService?.stopRecording()
    }

    /**
     * Остановка воспроизведения записи
     */
    private fun stopPlaying() {
        audioPlayerService?.stopPlaying()
    }

    /**
     * Запуск таймера продолжительности записи
     */
    private fun startListeningTimer() {
        audioRecorderService?.setOnTimerChangedListener(object :
            AudioRecorderService.OnTimerChangedListener {

            /**
             * Отображение изменений UI после начала записи
             */
            override fun onStartRecordingClicked() {
                isRecording = true
                updateRecordingUi()
            }

            /**
             * Получение показаний таймера из сервиса через интерфейс
             */
            override fun onTimerChanged(time: String) {
                timerTextView?.text = time
            }

            /**
             * Отображение изменений UI после окончания записи
             */
            override fun onStopRecordingClicked() {
                isRecording = false
                updateRecordingUi()
                updateRecordingsList()
            }
        })
    }

    /**
     * Запуск слушателя изменений в звуковоспроизводящем сервисе
     */
    private fun startListeningPlayback() {
        audioPlayerService?.setPlayerServiceListener(object :
            AudioPlayerService.IOnPlayerServiceListener {

            /**
             * Получение значения длительности записи и отображение изменений UI
             */
            override fun onPlayStarted(duration: Int) {
                showPlayingUI()
                progressBar?.max = duration
            }

            /**
             * Получение позиции прогресса воспроизведения записи
             */
            override fun onPlayProgressChanged(currentPosition: Int) {
                progressBar?.progress = currentPosition
            }

            /**
             * Отображение изменений UI после окончания воспроизведения записи
             */
            override fun onPlayStopped() {
                showRecordingUI()
            }
        })
    }

    /**
     * Отображение звукопроигрывающего UI
     */
    private fun showPlayingUI() {
        recordButton?.visibility = GONE
        timerTextView?.visibility = GONE
        progressBar?.visibility = VISIBLE
        stopPlayingButton?.visibility = VISIBLE
    }

    /**
     * Отображение звукозаписывающего UI
     */
    private fun showRecordingUI() {
        progressBar?.visibility = GONE
        stopPlayingButton?.visibility = GONE
        recordButton?.visibility = VISIBLE
        timerTextView?.visibility = VISIBLE
    }

    /**
     * Класс [ServiceConnection] возвращает обьект [AudioRecorderService]
     */
    inner class AudioRecorderServiceConnection : ServiceConnection {

        /**
         * Установление соединения со звукозаписывающим сервисом
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            audioRecorderService =
                (service as AudioRecorderService.LocalAudioRecorderServiceBinder).getAudioRecorderService()
            startListeningTimer()
        }

        /**
         * Разрыв соединения со звукозаписывающим сервисом
         */
        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }

    /**
     * Привязывание звукозаписывающего сервиса к текущей Activity
     */
    private fun bindRecordingService() {
        val intent = Intent(this@MainActivity, AudioRecorderService::class.java)
        audioRecorderServiceConnection?.let { bindService(intent, it, BIND_AUTO_CREATE) }
    }

    /**
     * Отвязывание звукозаписывающего сервиса к текущей Activity
     */
    private fun unbindRecordingService() {
        if (audioRecorderServiceConnection != null && audioRecorderService != null) {
            unbindService(audioRecorderServiceConnection!!)
        }
    }


    /**
     * Класс [ServiceConnection] возвращает обьект [AudioPlayerService]
     */
    inner class AudioPlayerServiceConnection : ServiceConnection {

        /**
         * Установление соединения с звуковоспроизводящим сервисом
         */
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            audioPlayerService =
                (service as AudioPlayerService.LocalAudioPlayerServiceBinder).getAudioPlayerService()
            startListeningPlayback()
        }

        /**
         * Разрыв соединения с звуковоспроизводящим сервисом
         */
        override fun onServiceDisconnected(name: ComponentName?) {
        }

    }

    /**
     * Привязывание звуковоспроизводящего сервиса к текущей Activity
     */
    private fun bindPlayerService() {
        val intent = Intent(this@MainActivity, AudioPlayerService::class.java)
        audioPlayerServiceConnection?.let { bindService(intent, it, BIND_AUTO_CREATE) }
    }

    /**
     * Отвязывание звуковоспроизводящего сервиса к текущей Activity
     */
    private fun unbindPlayerService() {
        if (audioPlayerServiceConnection != null && audioPlayerService != null) {
            unbindService(audioPlayerServiceConnection!!)
        }
    }

    /**
     * Завершение активных процессов
     */
    override fun onDestroy() {
        unbindRecordingService()
        unbindPlayerService()
        super.onDestroy()
    }

}