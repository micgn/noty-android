package de.mg.noty.model

import android.arch.persistence.room.*
import android.support.annotation.NonNull
import java.time.LocalDate
import java.util.*

@Entity(
    indices = [Index(value = ["text"], unique = false)]
)
data class Note(

    @PrimaryKey
    @NonNull
    @ColumnInfo
    var id: String = UUID.randomUUID().toString(),

    @NonNull
    @ColumnInfo
    var text: String,

    @ColumnInfo
    var dueDate: LocalDate? = null,

    @ColumnInfo
    var lastEdit: Long = System.currentTimeMillis()
) {

    @Ignore
    var tags: MutableList<Tag> = mutableListOf()


    fun toggleTag(tag: Tag) {
        if (tags.contains(tag)) tags.remove(tag)
        else tags.add(tag)
    }

    fun copy(): Note {
        val note = Note(id, text, dueDate, lastEdit)
        note.tags = tags
        return note
    }

    fun isDue(): Boolean =
        dueDate != null && (dueDate!!.isEqual(LocalDate.now()) || dueDate!!.isBefore(LocalDate.now()))
}