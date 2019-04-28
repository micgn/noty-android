package de.mg.noty.ui.noteslist


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.mg.noty.R
import de.mg.noty.model.Note
import de.mg.noty.ui.NotyViewModel
import kotlinx.android.synthetic.main.fragment_note.view.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class NotesListViewAdapter(
    private val listener: OnNoteListItemListener?,
    private val viewModel: NotyViewModel
) : RecyclerView.Adapter<NotesListViewAdapter.ViewHolder>() {

    private var values = emptyList<Note>()

    private val onClickListener: View.OnClickListener
    private val onLongClickListener: View.OnLongClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val note = v.tag as Note
            viewModel.selectedNote = note.copy()
            listener?.onItemClick(note, false)
        }
        onLongClickListener = View.OnLongClickListener { v ->
            val note = v.tag as Note
            viewModel.selectedNote = note.copy()
            listener?.onItemClick(note, true)
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = values[position]
        holder.noteText.text = note.text
        holder.lastEdit.text = formatDate(note.lastEdit)
        holder.dueDate.text = if (note.dueDate != null) "→ ${formatDate(note.dueDate!!)}" else ""
        holder.noteTags.text = note.tags.joinToString("\n") { t -> t.name }

        with(holder.view) {
            tag = note
            setOnClickListener(onClickListener)
            setOnLongClickListener(onLongClickListener)
        }
    }

    private fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("dd.MM.YY"))

    private fun formatDate(date: Long): String =
        formatDate(Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate())

    override fun getItemCount(): Int = values.size

    fun setValues(values: List<Note>?) {
        this.values = values.orEmpty()
        notifyDataSetChanged()
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val noteText: TextView = view.noteText
        val lastEdit: TextView = view.noteLastEdit
        val dueDate: TextView = view.noteDueDate
        val noteTags: TextView = view.noteTags
    }
}
