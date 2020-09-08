package com.cazapp.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.cazapp.R
import com.cazapp.localdb.LocalSong
import com.cazapp.localdb.LocalSongDao
import com.cazapp.localdb.SavedSong
import com.cazapp.localdb.SavedSongDao
import java.util.*


class SavedSongAdapter(var songList: List<SavedSong>, val savedSongDao: SavedSongDao, val listener: (SavedSong) -> Unit) :
    androidx.recyclerview.widget.RecyclerView.Adapter<SavedSongAdapter.SavedSongHolder>(){

    class SavedSongHolder(val view : View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view){

        private var textView : TextView = view.findViewById(R.id.title_saved_song_holder)


        @SuppressLint("ClickableViewAccessibility")
        fun bindSavedSong(savedSong: SavedSong, listener: (SavedSong) -> Unit){
            textView.text = savedSong.title
            textView.setOnClickListener{listener(savedSong)}

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSongHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.saved_song_holder,parent,false)
        return SavedSongHolder(textView)
    }

    override fun onBindViewHolder(holder: SavedSongHolder, position: Int) {
        holder.bindSavedSong(songList[position], listener)
    }

    override fun getItemCount() : Int = songList.size

}