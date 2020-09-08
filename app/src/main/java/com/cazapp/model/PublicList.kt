package com.cazapp.model

import com.cazapp.localdb.ListLocalSong
import com.cazapp.localdb.LocalSong
import org.bson.Document
import org.bson.types.ObjectId
import java.time.LocalDate
import java.util.*

class PublicList(var _id: ObjectId?, var title: String, var createdAt: Date?,var visible: Boolean, var songs: List<PublicSong>) {

    constructor():this(null,"", null,true, listOf())

    override fun toString(): String {
        return title
    }

    fun toStringParse(): String {
        var stringPublicList = "${_id!!.toHexString()}|$title|$createdAt|$visible"
        for (song in songs){
            stringPublicList += "\n${song.toStringParse()}"
        }
        return stringPublicList
    }

    fun toDocument(): Document {
        val docPublicList = Document().append("_id",_id)
        docPublicList.append("title",title)
            .append("createdAt",createdAt)
            .append("visible",visible)

        val docSongs = mutableListOf<Document>()
        for (song in songs){
            docSongs.add(song.toDocument())
        }
        docPublicList.append("songs",docSongs)
        return docPublicList
    }

    fun toLocalList(newId: Boolean): ListLocalSong {
        var id = _id
        if (newId){
            id = ObjectId()
        }
        return ListLocalSong(id!!.toHexString(),title)
    }

    fun toLocalSong(idList: String): List<LocalSong>{
        val localSongs = mutableListOf<LocalSong>()
        var localSong : LocalSong
        for (publicSong in songs){
            localSong = publicSong.toLocalSong()
            localSong.list_id = idList
            localSongs.add(localSong)

        }
        return localSongs
    }

    companion object {
        fun localListToPublicList(localList: ListLocalSong, localSongs: List<LocalSong>) : PublicList{
            val publicSongs = mutableListOf<PublicSong>()
            for (localSong in localSongs){
                publicSongs.add(PublicSong.localSongToPublicSong(localSong))
            }
            return PublicList(ObjectId(localList.id),localList.title,Date(),true,publicSongs)
        }

        fun toPublicList(s: String): PublicList {
            val fields = s.split("\n").toMutableList()
            val fieldsPublicList = fields.removeAt(0).split("|")
            val publicSongs = mutableListOf<PublicSong>()
            for (sPublicSong in fields){
                publicSongs.add(PublicSong.toPublicSong(sPublicSong))
            }
            return PublicList(ObjectId(fieldsPublicList[0]),fieldsPublicList[1],Date(fieldsPublicList[2]),true,publicSongs)
        }
    }
}