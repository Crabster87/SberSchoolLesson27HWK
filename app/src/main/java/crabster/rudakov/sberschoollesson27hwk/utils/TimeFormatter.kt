package crabster.rudakov.sberschoollesson27hwk.utils

import java.util.concurrent.TimeUnit


/**
 * Утилитарный класс дял форматирования времени
 */
object TimeFormatter {

    /**
     * Форматирует время из миллисекунд в формат HH:MM:SS
     *
     * @param time время в миллисекундах
     *
     * @return возвращает строку в формате 23:59:00
     */
    fun formatTime(time: Long): String {
        var millisecs = time
        val hours = TimeUnit.MILLISECONDS.toHours(millisecs)
        millisecs -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisecs)
        millisecs -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisecs)

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
    }

}