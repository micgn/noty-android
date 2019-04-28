package de.mg.noty.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import de.mg.noty.model.NotesTagsJoin

@Dao
interface NotesTagsJoinDao {

    @Query("SELECT * FROM NotesTagsJoin")
    fun findAll(): LiveData<List<NotesTagsJoin>>

    @Query("SELECT * FROM NotesTagsJoin WHERE noteId = :noteId AND tagId = :tagId")
    fun find(noteId: String, tagId: String): NotesTagsJoin?

    @Query("SELECT * FROM NotesTagsJoin WHERE noteId = :noteId")
    fun findByNote(noteId: String): List<NotesTagsJoin>

    @Query("SELECT * FROM NotesTagsJoin WHERE tagId = :tagId")
    fun findByTag(tagId: String): List<NotesTagsJoin>

    @Insert
    fun insert(relation: NotesTagsJoin)

    @Query("DELETE FROM NotesTagsJoin WHERE noteId = :noteId AND tagId = :tagId")
    fun delete(noteId: String, tagId: String)

    @Query("DELETE FROM NotesTagsJoin WHERE noteId = :noteId")
    fun deleteNote(noteId: String)

    @Query("DELETE FROM NotesTagsJoin WHERE tagId = :tagId")
    fun deleteTag(tagId: String)
}