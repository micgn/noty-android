package de.mg.noty.model

import android.arch.persistence.room.*
import android.support.annotation.NonNull


@Entity(
    foreignKeys = [ForeignKey(
        entity = Note::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Tag::class,
        parentColumns = ["id"],
        childColumns = ["tagId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["noteId"], unique = false),
        Index(value = ["tagId"], unique = false),
        Index(value = ["noteId", "tagId"], unique = true)]
)
data class NotesTagsJoin(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo
    var id: Long? = null,

    @NonNull
    var noteId: String,

    @NonNull
    var tagId: String,

    @ColumnInfo
    var lastEdit: Long = System.currentTimeMillis()
)