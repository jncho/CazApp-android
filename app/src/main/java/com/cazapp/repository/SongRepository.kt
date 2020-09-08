package com.cazapp.repository

import com.cazapp.model.PublicList
import com.cazapp.model.PublicSong
import com.cazapp.model.Song
import org.bson.types.ObjectId


interface SongRepository {
    fun getSongs(query : String, searchContent : Boolean, callback: ResultCallback<List<Song>>)
    fun getSong(_id : String , callback : ResultCallback<Song>)
    fun createList(listSong : PublicList, callback : ResultCallback<Boolean>)
    fun getLists(callback: ResultCallback<List<PublicList>>)
    fun updateList(publicList: PublicList , callback : ResultCallback<Boolean>)
    fun deleteList(_id : String, callback : ResultCallback<Boolean>)
    fun isLoggedIn() : Boolean
    fun getList(_id: String, callback: SongRepository.ResultCallback<PublicList?>)
    fun getSongsById(ids:List<String>,callback: SongRepository.ResultCallback<List<Song>>)

    interface ResultCallback<T>{
        fun onPreExecute()
        fun onSuccessExecute(result: T)
        fun onFailureExecute()
    }

}