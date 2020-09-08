package com.cazapp.localdb

import androidx.room.*

@Dao
interface ListLocalSongDao {

    @Insert
    fun insert(listLocalSong: ListLocalSong) : Long

    @Delete
    fun delete(listLocalSong: ListLocalSong)

    @Query("SELECT * FROM ListLocalSong WHERE id==:idList")
    fun get(idList: String): ListLocalSong?

    @Query("SELECT * FROM ListLocalSong")
    fun getAll(): List<ListLocalSong>

    @Query("SELECT COUNT(*) FROM ListLocalSong")
    fun size(): Int

    @Update
    fun update(listLocalSong: ListLocalSong)

}