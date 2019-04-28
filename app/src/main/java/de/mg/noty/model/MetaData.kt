package de.mg.noty.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity
data class MetaData(

    @PrimaryKey
    @NonNull
    @ColumnInfo
    val id: Long = 1L,

    @NonNull
    @ColumnInfo
    var lastReceivedServerDelta: Long
)