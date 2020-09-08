package com.cazapp.localdb

import androidx.room.*

@Dao
interface SavedSongDao {
    @Insert
    fun insert(savedSong: SavedSong)

    @Insert
    fun insertAll(savedSongs: List<SavedSong>)

    @Update
    fun update(savedSong: SavedSong)

    @Update
    fun updateLocalSongs(savedSongs : List<SavedSong>)

    @Delete
    fun delete(savedSong: SavedSong)

    @Delete
    fun deleteAll(savedSongs: List<SavedSong>)

    @Query("SELECT * FROM SavedSong WHERE _id==:idSavedSong")
    fun get(idSavedSong: String): SavedSong?

    @Query("SELECT * FROM SavedSong WHERE favorite==1")
    fun getFavorites() : List<SavedSong>
}