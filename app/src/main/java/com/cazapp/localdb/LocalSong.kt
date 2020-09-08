package com.cazapp.localdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(foreignKeys = arrayOf(ForeignKey(
    entity = ListLocalSong::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("list_id"),
    onDelete = CASCADE)),
    primaryKeys = arrayOf("_id","list_id"))
class LocalSong (
    val _id: String,
    var list_id: String,
    @ColumnInfo var title: String,
    @ColumnInfo var alt_title: String,
    @ColumnInfo var semitones: Int,
    @ColumnInfo var priority: Int,
    @ColumnInfo var textSize: Float = 15f
) {
    override fun toString(): String {
        return "$_id|$list_id|$title|$alt_title|$semitones|$priority|$textSize"
    }

    companion object {
        fun toLocalSong(s: String) : LocalSong{
            val fields = s.split("|")
            return LocalSong(fields[0],fields[1],fields[2],fields[3],fields[4].toInt(),fields[5].toInt(),fields[6].toFloat())
        }
    }
}