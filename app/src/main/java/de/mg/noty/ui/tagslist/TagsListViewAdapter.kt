package de.mg.noty.ui.tagslist


import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import de.mg.noty.R
import de.mg.noty.model.Tag
import de.mg.noty.ui.NotyViewModel
import de.mg.noty.ui.NotyViewModel.SelectedViewEnum.NoteEdit
import de.mg.noty.ui.NotyViewModel.SelectedViewEnum.Overview
import kotlinx.android.synthetic.main.fragment_tag.view.*


class TagsListViewAdapter(
    private val listener: OnTagListItemListener?,
    private val viewModel: NotyViewModel
) : RecyclerView.Adapter<TagsListViewAdapter.ViewHolder>() {

    private var tagList = emptyList<Tag>()
    private var noteCounts = emptyList<Int>()

    private val onClickListener: View.OnClickListener
    private val onLongClickListener: View.OnLongClickListener

    fun notifyChange() {
        this.tagList = viewModel.allTags.value.orEmpty()
        this.noteCounts =
            tagList.map { tag ->
                viewModel.allNotes.value.orEmpty().filter {
                    it.tags.contains(tag) && (!viewModel.getHideNotDue() || it.dueDate == null || it.isDue())
                }.count()
            }
        notifyDataSetChanged()
    }

    init {
        onClickListener = View.OnClickListener { v ->
            val tag = v.tag as Tag
            if (viewModel.currentView == Overview) {
                tag.selected = !tag.selected
            } else if (viewModel.currentView == NoteEdit) {
                if (viewModel.selectedNote != null)
                    viewModel.selectedNote!!.toggleTag(tag)
                else if (viewModel.newNote != null)
                    viewModel.newNote!!.toggleTag(tag)
            }
            // no update always needed, but a notify
            viewModel.update(tag)
        }
        onLongClickListener = View.OnLongClickListener { v ->
            val tag = v.tag as Tag
            viewModel.selectedTag = tag
            listener?.onItemClick(tag, true)
            true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentTag = tagList[position]

        holder.tagNameTextView.text =
            if (containsDue(currentTag))
                Html.fromHtml("<font color=\"red\"><sup>*</sup></font> ${currentTag.name}", 0)
            else
                currentTag.name

        holder.notesPerTag.text = this.noteCounts[position].toString()
        val tagContainer = holder.tagNameTextView.parent as LinearLayout
        tagContainer.setBackgroundResource(
            if (showTagAsSelected(currentTag)) R.drawable.tag_background_selected else R.drawable.tag_background
        )

        with(holder.view) {
            tag = currentTag
            setOnClickListener(onClickListener)
            setOnLongClickListener(onLongClickListener)
        }
    }

    private fun containsDue(tag: Tag): Boolean {
        return viewModel.allNotes.value.orEmpty()
            .any { note ->
                note.tags.contains(tag) && note.isDue()
            }
    }

    private fun showTagAsSelected(tag: Tag): Boolean {
        val currentNote = if (viewModel.selectedNote != null) viewModel.selectedNote else viewModel.newNote
        return (viewModel.currentView == NoteEdit && currentNote != null && currentNote.tags.contains(tag)) ||
                (viewModel.currentView == Overview && tag.selected)
    }

    override fun getItemCount(): Int = tagList.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tagNameTextView: TextView = view.tagName
        val notesPerTag: TextView = view.notesPerTag

        override fun toString(): String {
            return super.toString() + " '" + tagNameTextView.text + "'"
        }
    }
}
