package crabster.rudakov.sberschoollesson27hwk.view

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import crabster.rudakov.sberschoollesson27hwk.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Класс отвечающий за запись звука с микрофона
 */
class MediaRecorderWrapper(private val context: Context) {

    private var fileName: String = ""
    private var recorder: MediaRecorder? = null

    /**
     * Начать запись
     */
    fun startRecording() {
        fileName = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath}/${getFilename()}"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("MediaRecorderWrapper", "prepare() failed")
            }
            start()
        }
    }

    /**
     * Остановить запись
     */
    fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    /**
     * Создать имя файла записи
     */
    private fun getFilename(): String {
        val dateFormat = SimpleDateFormat(context.getString(R.string.file_name_format), Locale.getDefault())
        val currentDate = Calendar.getInstance()
        return "${dateFormat.format(currentDate.time)}.aac"
    }

}