package com.cazapp.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cazapp.R
import com.cazapp.model.PublicSong


class PublicSongAdapter(var songList: List<PublicSong>, val listener: (PublicSong) -> Unit) :
    androidx.recyclerview.widget.RecyclerView.Adapter<PublicSongAdapter.PublicSongHolder>() {


    class PublicSongHolder(val view : View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private var textView : TextView = view.findViewById(R.id.title_public_song_holder)

        @SuppressLint("ClickableViewAccessibility")
        fun bindLocalSong(publicSong: PublicSong, listener: (PublicSong) -> Unit){
            textView.text = publicSong.alt_title
            textView.setOnClickListener{listener(publicSong)}

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicSongHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.public_song_holder,parent,false)
        return PublicSongHolder(textView)
    }

    override fun onBindViewHolder(holder: PublicSongHolder, position: Int) {
        holder.bindLocalSong(songList[position], listener)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() : Int = songList.size

}