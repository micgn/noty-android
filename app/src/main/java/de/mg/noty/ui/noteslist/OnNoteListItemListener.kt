package de.mg.noty.ui.noteslist

import de.mg.noty.model.Note

interface OnNoteListItemListener {

    fun onItemClick(note: Note, longClick: Boolean = false)
}