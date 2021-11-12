package crabster.rudakov.sberschoollesson27hwk.adapter

/**
 * Интерфейс слушателя нажатий на элемент списка на главном экране
 */
interface IRecyclerListener {

    /**
     * Вызывается при нажатии на элемент списка
     *
     * @param pathFile путь к нажатому файлу
     */
    fun onRecordingClick(pathFile: String) {
    }

}