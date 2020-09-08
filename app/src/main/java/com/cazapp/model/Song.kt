package com.cazapp.model

import com.cazapp.localdb.SavedSong
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bson.types.ObjectId

open class Song(var _id: ObjectId?, var title: String, var section: String, var body: List<Line>) {

    constructor() : this(null, "", "", listOf())

    override fun toString(): String {
        return title
    }

    fun transpose(semitone: Int) {
        if (semitone != 0) {
            for (line in body) {
                line.transpose(semitone)
            }
        }
    }

    companion object {
        fun toSong(savedSong: SavedSong): Song {
            val gson = Gson()
            val lineListType = object : TypeToken<List<Line>>() {}.type
            val bodyL: List<Line> = gson.fromJson(savedSong.body, lineListType)
            return Song(ObjectId(savedSong._id),savedSong.title,savedSong.section,bodyL)
        }
    }

}