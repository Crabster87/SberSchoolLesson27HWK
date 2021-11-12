package crabster.rudakov.sberschoollesson27hwk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import crabster.rudakov.sberschoollesson27hwk.R
import java.io.File


/**
 * Адаптер для списка записей на главном экране
 *
 * @param listener сушатель нажатий на элемент списка
 */
class RecordingsRecyclerAdapter(private var listener: IRecyclerListener) :
    RecyclerView.Adapter<RecordingsRecyclerAdapter.RecordingViewHolder>() {

    private var data: List<File> = mutableListOf()

    /**
     * Создает держатель View
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recordings_item, parent, false)
        return RecordingViewHolder(view)
    }

    /**
     * Привязывает View к позиции списка
     */
    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemView.setOnClickListener {
            listener.onRecordingClick(data[position].absolutePath)
        }
    }

    /**
     * Возвращает размер списка файлов
     */
    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * Передает список файлов для отображения
     */
    fun setData(list: List<File>) {
        data = list
        notifyDataSetChanged()
    }

    /**
     * Класс [RecordingViewHolder] устанавливает держателя View каждого элемента
     */
    class RecordingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.recording_name_textView)

        /**
         * Устанавливает имя файла в соответствующее View каждого элемента списка
         */
        fun bind(file: File) {
            nameTextView.text = file.name
        }

    }

}