package de.mg.noty.db.dao


import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import de.mg.noty.model.Note
import java.time.LocalDate

@Dao
interface NoteDao {

    @Query("SELECT * FROM note ORDER BY text ASC")
    fun findAll(): LiveData<List<Note>>

    @Query("SELECT * FROM note WHERE dueDate <= :now ORDER BY dueDate DESC")
    fun findAllDue(now: LocalDate): List<Note>

    @Query("SELECT * FROM note WHERE text = :text")
    fun findNoteByText(text: String): Note?

    @Query("SELECT * FROM note WHERE id = :id")
    fun findById(id: String): Note?

    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Query("DELETE FROM note")
    fun deleteAll()

    @Query("DELETE FROM note WHERE id = :noteId")
    fun delete(noteId: String)


}
