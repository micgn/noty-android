package de.mg.noty.db.dao


import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import de.mg.noty.model.Tag

@Dao
interface TagDao {

    @Query("SELECT * FROM tag ORDER BY name ASC")
    fun findAll(): LiveData<List<Tag>>

    @Query("SELECT * FROM tag WHERE name =:name")
    fun findTagByName(name: String): Tag?

    @Query("SELECT * FROM tag WHERE id = :id")
    fun findById(id: String): Tag?

    @Insert
    fun insert(tag: Tag)

    @Update
    fun update(tag: Tag)

    @Query("DELETE FROM tag")
    fun deleteAll()

    @Query("DELETE FROM tag WHERE id = :tagId")
    fun delete(tagId: String)
}
