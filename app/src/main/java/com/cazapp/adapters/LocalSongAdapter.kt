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
import java.util.*


class LocalSongAdapter(var songList: List<LocalSong>, val localSongDao: LocalSongDao, val listenerDrag : OnStartDragListener, val listener: (LocalSong) -> Unit) :
    androidx.recyclerview.widget.RecyclerView.Adapter<LocalSongAdapter.LocalSongHolder>(), TouchHelperCallback.ItemTouchHelperAdapter {

    interface OnStartDragListener{
        fun onStartDrag(viewHolder : androidx.recyclerview.widget.RecyclerView.ViewHolder)
    }

    class LocalSongHolder(val view : View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) , TouchHelperCallback.ItemTouchHelperViewHolder{

        private var textView : TextView = view.findViewById(R.id.title_local_song_holder)
        private var dragView : ImageView = view.findViewById(R.id.drag_song)


        @SuppressLint("ClickableViewAccessibility")
        fun bindLocalSong(localSong: LocalSong, listenerDrag : OnStartDragListener, listener: (LocalSong) -> Unit){
            textView.text = localSong.alt_title
            textView.setOnClickListener{listener(localSong)}
            dragView.setOnTouchListener { _, event ->
                if (event.getAction()
                    == MotionEvent.ACTION_DOWN) {
                    // Notify ItemTouchHelper to start dragging
                    listenerDrag.onStartDrag(this)
                }
                false
            }
        }

        override fun onItemClear() {
            view.setBackgroundColor(Color.parseColor("#FFE6E6E6"))
        }

        override fun onItemSelected() {
            view.setBackgroundColor(Color.parseColor("#FFD9D9D9"))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalSongHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.local_song_holder,parent,false)
        return LocalSongHolder(textView)
    }

    override fun onBindViewHolder(holder: LocalSongHolder, position: Int) {
        holder.bindLocalSong(songList[position],listenerDrag, listener)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() : Int = songList.size

    override fun onItemMove(fromPosition: Int, toPosition: Int) : Boolean{
        var swap : Int
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                swap = songList[i+1].priority
                songList[i+1].priority = songList[i].priority
                songList[i].priority = swap
                Collections.swap(songList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                swap = songList[i-1].priority
                songList[i-1].priority = songList[i].priority
                songList[i].priority = swap
                Collections.swap(songList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun updateSongs(){
        localSongDao.updateLocalSongs(songList)
    }
}