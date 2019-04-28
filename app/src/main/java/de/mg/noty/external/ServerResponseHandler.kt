package de.mg.noty.external

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import de.mg.noty.db.NotyDatabase
import de.mg.noty.db.dao.MetaDataDao
import de.mg.noty.db.dao.NoteDao
import de.mg.noty.db.dao.NotesTagsJoinDao
import de.mg.noty.db.dao.TagDao
import de.mg.noty.external.dto.ActionEnum.*
import de.mg.noty.external.dto.DtoMapper
import de.mg.noty.external.dto.ResponseDto
import de.mg.noty.model.NotesTagsJoin

class ServerResponseHandler(appContext: Context) {

    private val mapper = ObjectMapper()
    private val db: NotyDatabase = NotyDatabase.getDatabase(appContext)!!

    private val noteDao: NoteDao
    private val tagDao: TagDao
    private val notesTagsJoinDao: NotesTagsJoinDao
    private val metaDataDao: MetaDataDao

    init {
        noteDao = db.NoteDao()
        tagDao = db.TagDao()
        notesTagsJoinDao = db.NotesTagsJoinDao()
        metaDataDao = db.MetaDataDao()
    }

    fun handle(responseStr: String) {

        val response = mapper.readValue<ResponseDto>(responseStr, ResponseDto::class.javaObjectType)

        // the server does always win, no checking for conflicts here
        response.newDeltas.sortedBy { it.getUpdated() }.forEach { delta ->

            val action = delta.action
            when {
                delta.note != null -> {
                    val noteDto = delta.note
                    when (action) {
                        CREATE -> {
                            notesTagsJoinDao.deleteNote(noteDto.noteId)
                            noteDao.delete(noteDto.noteId)
                            noteDao.insert(DtoMapper.map(noteDto))
                        }
                        UPDATE -> {
                            val savedNote = noteDao.findById(noteDto.noteId)
                            if (savedNote == null) {
                                noteDao.insert(DtoMapper.map(noteDto))
                            } else {
                                noteDao.update(DtoMapper.map(noteDto))
                            }
                        }
                        DELETE -> {
                            notesTagsJoinDao.deleteNote(noteDto.noteId)
                            noteDao.delete(noteDto.noteId)
                        }
                    }
                }
                delta.tag != null -> {
                    val tagDto = delta.tag
                    when (action) {
                        CREATE -> {
                            notesTagsJoinDao.deleteTag(tagDto.tagId)
                            tagDao.delete(tagDto.tagId)
                            tagDao.insert(DtoMapper.map(tagDto, false))
                        }
                        UPDATE -> {
                            val savedTag = tagDao.findById(tagDto.tagId)
                            if (savedTag == null) {
                                tagDao.insert(DtoMapper.map(tagDto, false))
                            } else {
                                tagDao.update(DtoMapper.map(tagDto, savedTag.selected))
                            }
                        }
                        DELETE -> {
                            notesTagsJoinDao.deleteTag(tagDto.tagId)
                            tagDao.delete(tagDto.tagId)
                        }
                    }
                }
                delta.noteTag != null -> {
                    val noteTag = delta.noteTag
                    when (action) {
                        CREATE -> {
                            if (notesTagsJoinDao.find(noteTag.noteId, noteTag.tagId) == null &&
                                noteDao.findById(noteTag.noteId) != null &&
                                tagDao.findById(noteTag.tagId) != null
                            )
                                notesTagsJoinDao.insert(NotesTagsJoin(noteId = noteTag.noteId, tagId = noteTag.tagId))
                        }
                        UPDATE -> throw RuntimeException("received delta for noteTag and action UPDATE")
                        DELETE -> {
                            notesTagsJoinDao.delete(noteTag.noteId, noteTag.tagId)
                        }
                    }
                }
                else -> throw RuntimeException("received empty delta")
            }
        }

        val metaData = metaDataDao.get()
        // meta data have been inserted before
        metaData!!.lastReceivedServerDelta = System.currentTimeMillis()
        metaDataDao.update(metaData)
    }
}