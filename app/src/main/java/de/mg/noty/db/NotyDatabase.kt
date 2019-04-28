package de.mg.noty.db


import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import de.mg.noty.db.dao.MetaDataDao
import de.mg.noty.db.dao.NoteDao
import de.mg.noty.db.dao.NotesTagsJoinDao
import de.mg.noty.db.dao.TagDao
import de.mg.noty.model.MetaData
import de.mg.noty.model.Note
import de.mg.noty.model.NotesTagsJoin
import de.mg.noty.model.Tag


@Database(entities = [Note::class, Tag::class, NotesTagsJoin::class, MetaData::class], version = 1)
@TypeConverters(LocalDateConverter::class)
abstract class NotyDatabase : RoomDatabase() {

    abstract fun NoteDao(): NoteDao
    abstract fun TagDao(): TagDao
    abstract fun NotesTagsJoinDao(): NotesTagsJoinDao
    abstract fun MetaDataDao(): MetaDataDao

    companion object {

        @Volatile
        private var INSTANCE: NotyDatabase? = null

        fun getDatabase(context: Context): NotyDatabase? {
            if (INSTANCE == null) {
                synchronized(NotyDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            NotyDatabase::class.java,
                            "note_database"
                        )
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration()
                            // not recommended:
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE
        }

    }

}
