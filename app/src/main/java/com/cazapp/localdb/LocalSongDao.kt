package com.cazapp.localdb

import androidx.room.*

@Dao
interface LocalSongDao {
    @Insert
    fun insert(localSong: LocalSong)

    @Insert
    fun insertAll(localSongs: List<LocalSong>)

    @Update
    fun update(localSong: LocalSong)

    @Update
    fun updateLocalSongs(localSongs : List<LocalSong>)

    @Delete
    fun delete(localSong: LocalSong)

    @Delete
    fun deleteAll(localSongs: List<LocalSong>)

    @Query("SELECT * FROM LocalSong WHERE _id==:idSong")
    fun getByIdSong(idSong: String): LocalSong?

    @Query("SELECT * FROM LocalSong WHERE list_id==:idList ORDER BY priority ASC")
    fun getAll(idList : String): List<LocalSong>

    @Query("SELECT priority FROM LocalSong WHERE list_id==:idList ORDER BY priority DESC LIMIT 1")
    fun getLastPriority(idList : String) : Int
}