package com.cazapp.fragments

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import android.view.*
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.adapters.SongAdapter
import com.cazapp.model.Song
import com.cazapp.repository.SongRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_songs.*


interface OnSongListInteractionListener {
    fun onSongSelected(song: Song)
}

class SongsFragment : Fragment(), OnSongListInteractionListener {

    var adapterSong: SongAdapter? = null
    lateinit var layoutSong : androidx.recyclerview.widget.LinearLayoutManager
    var searchContent = false

    var listState: Parcelable? = null

    companion object {
        const val LIST_STATE_KEY = "recycler_list_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listState = savedInstanceState?.getParcelable(LIST_STATE_KEY)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        listState = song_list?.layoutManager?.onSaveInstanceState()
        outState.putParcelable(LIST_STATE_KEY, listState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).actionbar.title = "Songs"

        if (adapterSong == null) {
            adapterSong = SongAdapter(listOf()) { song -> onSongSelected(song) }
        }
        layoutSong = androidx.recyclerview.widget.LinearLayoutManager(activity)

        // Recycler view
        song_list.apply {
            setHasFixedSize(true)
            layoutManager = layoutSong
            adapter = adapterSong
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_songs, menu)
        val mSearchView = menu.findItem(R.id.appSearchBar)?.actionView as SearchView
        mSearchView.queryHint = "Search"

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {

                val callback = object : SongRepository.ResultCallback<List<Song>> {
                    override fun onFailureExecute() {
                        loading_bar_songs.visibility = View.GONE
                        song_list.visibility = View.VISIBLE

                        Snackbar.make(song_list, "Connection lost", Snackbar.LENGTH_LONG).show()
                    }

                    override fun onPreExecute() {
                        // Animation of loading
                        loading_bar_songs.visibility = View.VISIBLE
                        song_list.visibility = View.INVISIBLE
                        return
                    }

                    override fun onSuccessExecute(result: List<Song>) {

                        loading_bar_songs.visibility = View.GONE
                        song_list.visibility = View.VISIBLE

                        adapterSong?.songList = result
                        adapterSong?.notifyDataSetChanged()
                        (activity as MainActivity).hideHeyboard()
                    }
                }

                val repo = (activity as MainActivity).repository
                repo!!.getSongs(query, searchContent,callback)

                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })


        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.flag_search_content -> {
            if (item.isChecked) {
                item.isChecked = false
                searchContent = false
            } else {
                item.isChecked = true
                searchContent = true
            }

            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSongSelected(song: Song) {
        val ft = activity?.supportFragmentManager?.beginTransaction()
        val fragment = SongFragment.newInstance(song)
        ft?.replace(R.id.fragment, fragment)?.addToBackStack("song")
        ft?.commit()
    }

    override fun onResume() {
        super.onResume()
        if (listState != null) {
            song_list.layoutManager?.onRestoreInstanceState(listState)
        }
    }

    override fun onPause() {
        (activity as MainActivity).hideHeyboard()
        super.onPause()
    }
}