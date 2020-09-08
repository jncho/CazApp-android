package com.cazapp.repository

import android.content.Context
import androidx.room.Room
import com.cazapp.localdb.*

object SongRepositoryFactory {

    fun createRepository(): SongRepository? {
        return StitchRepository()
    }

    fun createLocalRepository(context: Context) : LocalDataBase {
        return Room.databaseBuilder(context, LocalDataBase::class.java, "LocalDatabase").allowMainThreadQueries().fallbackToDestructiveMigration()
            .build()
    }
}