package de.mg.noty.ui


import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import de.mg.noty.db.NotyRepository
import de.mg.noty.model.Note
import de.mg.noty.model.Tag
import de.mg.noty.ui.NotyViewModel.SelectedViewEnum.Overview

class NotyViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: NotyRepository = NotyRepository(application)

    val filteredNotes: LiveData<List<Note>>
    val allTags: LiveData<List<Tag>>
    val allNotes: LiveData<List<Note>>


    init {
        allTags = repo.allTags
        filteredNotes = repo.filteredNotes
        allNotes = repo.allNotes
    }

    fun registerAnyChangeObserver(observer: () -> Unit) {
        repo.registerAnyChangeObserver(observer)
    }

    fun insert(note: Note) = repo.insert(note)
    fun update(note: Note) = repo.update(note)
    fun delete(note: Note) = repo.delete(note)

    fun insert(tag: Tag) = repo.insert(tag)
    fun update(tag: Tag) = repo.update(tag)
    fun delete(tag: Tag) = repo.delete(tag)

    var currentView: SelectedViewEnum = Overview

    enum class SelectedViewEnum {
        Overview, NoteEdit
    }

    var selectedNote: Note? = null
    var newNote: Note? = null

    fun createNewNote() {
        newNote = Note(text = "")
        allTags.value?.forEach {
            if (it.selected) newNote!!.toggleTag(it)
        }
    }

    var selectedTag: Tag? = null

    fun getMetaData() = repo.getMetaData()

    fun setHideNotDue(checked: Boolean) {
        repo.hideNotDue = checked
    }

    fun getHideNotDue() = repo.hideNotDue

    fun getSimilars(input: String): List<String> = repo.getSimilars(input)

    fun overwriteServerContent() {
        repo.overwriteServerContent()
    }

}
