package com.cazapp.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cazapp.R
import com.cazapp.model.PublicList

class PublicListSongAdapter(var publicLists: List<PublicList>, val listener: (PublicList) -> Unit) :
    androidx.recyclerview.widget.RecyclerView.Adapter<PublicListSongAdapter.PublicListHolder>(){

    class PublicListHolder(val view : View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {

        private var textView : TextView = view.findViewById(R.id.title_public_list_holder)

        fun bindListSong(publicList: PublicList, listener: (PublicList) -> Unit){
            textView.text = publicList.title
            textView.setOnClickListener{listener(publicList)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicListHolder {
        val textView = LayoutInflater.from(parent.context).inflate(R.layout.public_list_holder,parent,false)
        return PublicListHolder(textView)
    }

    override fun onBindViewHolder(holder: PublicListHolder, position: Int) {
        holder.bindListSong(publicLists[position], listener)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() : Int = publicLists.size
}