package de.mg.noty.db


import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import de.mg.noty.db.dao.MetaDataDao
import de.mg.noty.db.dao.NoteDao
import de.mg.noty.db.dao.NotesTagsJoinDao
import de.mg.noty.db.dao.TagDao
import de.mg.noty.external.CallServerWorkManager
import de.mg.noty.external.dto.DtoMapper
import de.mg.noty.external.dto.req.AllContentDto
import de.mg.noty.external.dto.req.NoteDeltaDto
import de.mg.noty.external.dto.req.NoteTagDeltaDto
import de.mg.noty.external.dto.req.TagDeltaDto
import de.mg.noty.model.Note
import de.mg.noty.model.NotesTagsJoin
import de.mg.noty.model.Tag
import java.time.LocalDate
import java.time.ZoneOffset


class NotyRepository(application: Application) {

    private val noteDao: NoteDao
    private val tagDao: TagDao
    private val notesTagsJoinDao: NotesTagsJoinDao
    private val metaDataDao: MetaDataDao
    private val similarityService: SimilarityService

    val allNotes: LiveData<List<Note>>
    private val joins: LiveData<List<NotesTagsJoin>>

    val allTags: LiveData<List<Tag>>
    val filteredNotes = MutableLiveData<List<Note>>()

    var hideNotDue: Boolean = true
        set(value) {
            field = value
            onChange()
        }


    private val db: NotyDatabase = NotyDatabase.getDatabase(application)!!
    private val callServer = CallServerWorkManager()

    init {
        noteDao = db.NoteDao()
        tagDao = db.TagDao()
        notesTagsJoinDao = db.NotesTagsJoinDao()
        metaDataDao = db.MetaDataDao()

        allNotes = noteDao.findAll()
        allTags = tagDao.findAll()
        joins = notesTagsJoinDao.findAll()

        // onChange is called too often
        allNotes.observeForever { onChange() }
        allTags.observeForever { onChange() }
        joins.observeForever { onChange() }

        similarityService = SimilarityService()
    }

    private val observers: MutableList<() -> Unit> = mutableListOf()

    fun registerAnyChangeObserver(observer: () -> Unit) {
        observers.add(observer)
    }

    private fun onChange() {
        val notesV = allNotes.value
        val tagsV = allTags.value
        val joinsV = joins.value

        if (notesV == null || tagsV == null || joinsV == null)
            return

        // call can arrive while in between deleting a tag and deleting a note to tag relation
        notesV.forEach { note ->
            val tags = joinsV.filter { join -> join.noteId == note.id }
                .map { join -> tagsV.firstOrNull { tag -> tag.id == join.tagId } }.filterNotNull()
            note.tags = tags.toMutableList()
        }

        // no synchronization needed since must be called only from the "main" thread
        this.filteredNotes.value = filterAndSortNotes(notesV, tagsV)

        observers.forEach { it() }
    }

    private fun filterAndSortNotes(notesV: List<Note>, tagsV: List<Tag>): List<Note> {

        fun beforeOrEqualNow(date: LocalDate) =
            date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now())

        fun sortOrder(note: Note): Long =
            if (note.dueDate != null && beforeOrEqualNow(note.dueDate!!))
                note.dueDate!!.plusYears(500).atStartOfDay().toInstant(ZoneOffset.UTC)
                    .toEpochMilli()
            else
                note.lastEdit

        val filteredNotes = notesV
            .filter { note ->
                note.tags.any { tag -> tag.selected } ||
                        (note.tags.isEmpty() && tagsV.all { !it.selected })
            }
            .filter { note ->
                !hideNotDue || note.dueDate == null || beforeOrEqualNow(note.dueDate!!)
            }
            .sortedWith(Comparator { n1, n2 ->
                val n1Order = sortOrder(n1)
                val n2Oder = sortOrder(n2)
                if (n1Order < n2Oder) 1 else if (n1Order > n2Oder) -1 else n1.text.compareTo(n2.text)
            })
        return filteredNotes
    }


    fun findAllDueNotes() = noteDao.findAllDue(LocalDate.now())

    fun insert(note: Note) {
        note.lastEdit = System.currentTimeMillis()
        Background {
            db.runInTransaction {
                if (noteDao.findNoteByText(note.text) == null) {
                    noteDao.insert(note)
                    callServer.create(DtoMapper.map(note))
                    note.tags.forEach { tag ->
                        val notesTagsJoin = NotesTagsJoin(noteId = note.id, tagId = tag.id)
                        notesTagsJoinDao.insert(notesTagsJoin)
                        callServer.create(DtoMapper.map(notesTagsJoin))
                    }
                }
            }
        }.execute()
    }

    fun update(note: Note) {
        note.lastEdit = System.currentTimeMillis()
        Background {
            db.runInTransaction {
                val savedNote = noteDao.findById(note.id)
                if (savedNote != null) {
                    noteDao.update(note)

                    if (savedNote.text != note.text || savedNote.dueDate != note.dueDate)
                        callServer.update(DtoMapper.map(note))

                    val savedNoteTags = notesTagsJoinDao.findByNote(note.id)
                    val noteTagsToDelete =
                        savedNoteTags.filter { savedNoteTag -> note.tags.none { tag -> tag.id == savedNoteTag.tagId } }
                    val tagsToInsert =
                        note.tags.filter { tag -> savedNoteTags.none { noteTag -> noteTag.tagId == tag.id } }

                    noteTagsToDelete.forEach {
                        notesTagsJoinDao.delete(it.noteId, it.tagId)
                        callServer.delete(NoteTagDeltaDto(it.noteId, it.tagId))
                    }
                    tagsToInsert.forEach {
                        notesTagsJoinDao.insert(NotesTagsJoin(noteId = note.id, tagId = it.id))
                        callServer.create(NoteTagDeltaDto(note.id, it.id))
                    }
                }
            }
        }.execute()
    }

    fun delete(note: Note) {
        note.lastEdit = System.currentTimeMillis()
        Background {
            db.runInTransaction {
                val savedNoteTags = notesTagsJoinDao.findByNote(note.id)
                savedNoteTags.forEach {
                    notesTagsJoinDao.delete(it.noteId, it.tagId)
                    callServer.delete(NoteTagDeltaDto(it.noteId, it.tagId))
                }

                noteDao.delete(note.id)
                callServer.delete(DtoMapper.map(note))
            }
        }.execute()
    }

    fun insert(tag: Tag) {
        tag.lastEdit = System.currentTimeMillis()
        Background {
            db.runInTransaction {
                if (tagDao.findTagByName(tag.name) == null) {
                    tagDao.insert(tag)
                    callServer.create(DtoMapper.map(tag))
                }
            }
        }.execute()
    }

    fun update(tag: Tag) {
        tag.lastEdit = System.currentTimeMillis()
        Background {
            val savedTag = tagDao.findById(tag.id)
            if (savedTag != null) {
                tagDao.update(tag)

                // called too often from above to make a server call each time
                if (savedTag.name != tag.name)
                    callServer.update(DtoMapper.map(tag))
            }
        }.execute()
    }

    fun delete(tag: Tag) {
        tag.lastEdit = System.currentTimeMillis()
        Background {
            db.runInTransaction {
                notesTagsJoinDao.findByTag(tag.id).forEach {
                    notesTagsJoinDao.delete(it.noteId, it.tagId)
                    callServer.delete(NoteTagDeltaDto(it.noteId, it.tagId))
                }

                tagDao.delete(tag.id)
                callServer.delete(DtoMapper.map(tag))
            }
        }.execute()
    }


    private class Background(val callback: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            callback()
            return null
        }
    }

    fun getMetaData() = metaDataDao.get()

    fun getSimilars(input: String): List<String> =
        similarityService.get(input, allNotes.value?.map { it.text })


    fun overwriteServerContent() {
        val notesV = allNotes.value
        val tagsV = allTags.value
        val joinsV = joins.value
        if (notesV == null || tagsV == null || joinsV == null)
            throw IllegalStateException()

        val content = AllContentDto(
            noteCreateDeltas = notesV.map { NoteDeltaDto(it.id, it.text, DtoMapper.map(it.dueDate)) },
            tagCreateDeltas = tagsV.map { TagDeltaDto(it.id, it.name) },
            noteTagCreateDeltas = joinsV.map { NoteTagDeltaDto(it.noteId, it.tagId) })

        callServer.overwriteAll(content)
    }

}
