package com.cazapp.localdb

import androidx.room.Entity
import com.cazapp.model.Song
import com.google.gson.Gson

@Entity(primaryKeys = arrayOf("_id"))
class SavedSong (
    val _id: String,
    val title: String,
    val section: String,
    val body: String,
    var favorite: Boolean
){
    companion object {
        fun toSavedSong(song: Song): SavedSong{
            val gson = Gson()
            val bodyL = mutableListOf<String>()
            for (line in song.body){
                bodyL.add( gson.toJson(line))
            }
            val body = bodyL.joinToString(",",prefix = "[",postfix = "]")
            return SavedSong(song._id!!.toHexString(),song.title,song.section,body,true)
        }
    }
}