package com.cazapp.model

import com.cazapp.localdb.LocalSong
import org.bson.Document
import org.bson.types.ObjectId

class PublicSong(var _id : ObjectId?, var alt_title: String, var semitones: Int, var priority: Int, var textSize: Float = 15f ,var idSong: ObjectId?){
    constructor():this(null,"",0,0,15f,null)

    override fun toString(): String {
        return alt_title
    }

    fun toStringParse(): String {
        return "${_id!!.toHexString()}|$alt_title|$semitones|$priority|$textSize|${idSong!!.toHexString()}"
    }

    fun toDocument() = Document().append("_id",_id)
            .append("alt_title",alt_title)
            .append("semitones",semitones)
            .append("priority",priority)
            .append("textSize",textSize)
            .append("idSong",idSong)

    fun toLocalSong(): LocalSong{
        return LocalSong(_id!!.toHexString(),"",alt_title,alt_title,semitones,priority,textSize)
    }

    companion object {
        fun localSongToPublicSong(localSong: LocalSong) : PublicSong{
            return PublicSong(ObjectId(localSong._id),localSong.alt_title,localSong.semitones,localSong.priority,localSong.textSize,ObjectId(localSong._id))
        }
        fun toPublicSong(s: String): PublicSong{
            val fields = s.split("|")
            return PublicSong(ObjectId(fields[0]),fields[1],fields[2].toInt(),fields[3].toInt(),fields[4].toFloat(),ObjectId(fields[5]))
        }
    }
}