package de.mg.noty.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import java.util.*

@Entity(
    indices = [Index(value = ["name"], unique = false)]
)
data class Tag(

    @PrimaryKey
    @NonNull
    @ColumnInfo
    var id: String = UUID.randomUUID().toString(),

    @NonNull
    @ColumnInfo
    var name: String,

    var selected: Boolean = false,

    @ColumnInfo
    var lastEdit: Long = System.currentTimeMillis()
)