package com.cazapp.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.cazapp.R
import com.cazapp.localdb.ListLocalSong
import com.mongodb.stitch.android.core.Stitch

class ListSongAdapter(var songList: List<ListLocalSong>, val listener: ListenerListLocalSong) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ListSongAdapter.ListSongHolder>() {

    interface ListenerListLocalSong {
        fun listenerSelect(listLocalSong: ListLocalSong)
    }

    class ListSongHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private var textView: TextView = view.findViewById(R.id.title_list_holder)


        fun bindListSong(listSong: ListLocalSong, listener: ListenerListLocalSong) {
            textView.text = listSong.title
            view.setOnClickListener { listener.listenerSelect(listSong) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListSongHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.list_holder, parent, false)
        return ListSongHolder(textView)
    }

    override fun onBindViewHolder(holder: ListSongHolder, position: Int) {
        holder.bindListSong(songList[position], listener)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int = songList.size
}