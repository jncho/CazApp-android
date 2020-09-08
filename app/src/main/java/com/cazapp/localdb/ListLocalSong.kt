package com.cazapp.localdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class ListLocalSong(
    @PrimaryKey var id: String,
    @ColumnInfo var title: String
){
    override fun toString(): String {
        return title
    }

    fun toStringParse(): String {
        return "$id|$title"
    }

    companion object {
        fun toListLocalSong(s: String) : ListLocalSong{
            val fields = s.split("|")
            return ListLocalSong(fields[0],fields[1])
        }
    }
}
