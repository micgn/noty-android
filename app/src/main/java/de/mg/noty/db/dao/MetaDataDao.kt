package de.mg.noty.db.dao


import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import de.mg.noty.model.MetaData

@Dao
interface MetaDataDao {

    @Query("SELECT * FROM metadata WHERE id = 1")
    fun get(): MetaData?

    @Insert
    fun insert(metaData: MetaData)

    @Update
    fun update(metaData: MetaData)

}
