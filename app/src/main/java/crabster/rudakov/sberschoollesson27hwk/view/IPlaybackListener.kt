package crabster.rudakov.sberschoollesson27hwk.view

/**
 * Интерфейс слушателя изменений статуса и прогресса воспроизведения записей
 */
interface IPlaybackListener {

    /**
     * Вызывается при начале воспроизведения
     *
     * @param duration длительность записи в миллисекундах
     */
    fun onPlayStarted(duration: Int)

    /**
     * Вызывается при изменении прогресса воспроизведения
     *
     * @param currentPos текущая позиция в миллисекундах
     */
    fun onProgressChanged(currentPos: Int, duration: Int)

    /**
     * Вызывается при окончаниий воспроизведения
     */
    fun onPlayStopped()

}