package com.cazapp.adapters

import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.cazapp.R
import com.cazapp.model.Song

class SongAdapter( var songList: List<Song>, val listener: (Song) -> Unit) :
    androidx.recyclerview.widget.RecyclerView.Adapter<SongAdapter.SongHolder>(){

    class SongHolder(val view : View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private var textView : TextView = view.findViewById(R.id.title_song_holder)
        private var songCardView: View = view.findViewById(R.id.song_card_view)
        private var sectionView: TextView = view.findViewById(R.id.section_song_holder)

        fun bindSong(song: Song, listener: (Song) -> Unit){
            textView.text = song.title
            sectionView.text = song.section
            songCardView.setOnClickListener{listener(song)}

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.song_holder,parent,false)
        return SongHolder(textView)
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        holder.bindSong(songList[position], listener)

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() : Int = songList.size
}