package com.cazapp.localdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(ListLocalSong::class,LocalSong::class,SavedSong::class), version = 3)
abstract class LocalDataBase : RoomDatabase(){
    abstract fun listLocalSongDao() : ListLocalSongDao
    abstract fun localSongDao() : LocalSongDao
    abstract fun savedSongDao() : SavedSongDao
}