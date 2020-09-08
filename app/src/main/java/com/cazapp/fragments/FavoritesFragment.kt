package com.cazapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.adapters.SavedSongAdapter
import com.cazapp.localdb.LocalDataBase
import com.cazapp.localdb.SavedSong
import com.cazapp.localdb.SavedSongDao
import com.cazapp.repository.SongRepositoryFactory
import kotlinx.android.synthetic.main.fragment_favorites.*

class FavoritesFragment : Fragment() {

    private lateinit var savedSongDao: SavedSongDao
    private lateinit var db: LocalDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get List
        db = SongRepositoryFactory.createLocalRepository(activity!!)
        savedSongDao = db.savedSongDao()

        (activity as MainActivity).actionbar.title = "Favorites"

        // Recycler view
        saved_songs_list.apply {
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            update { savedSong -> onSavedSongSelected(savedSong) }
        }
    }

    override fun onResume() {
        super.onResume()
        saved_songs_list.update { savedSong -> onSavedSongSelected(savedSong) }
    }

    fun androidx.recyclerview.widget.RecyclerView.update(
        listener: (SavedSong) -> Unit
    ) {
        val songs = savedSongDao.getFavorites().toMutableList()
        if (adapter == null) {
            adapter = SavedSongAdapter(songs, savedSongDao, listener)
        } else {
            (adapter as SavedSongAdapter).songList = songs
            (adapter as SavedSongAdapter).notifyDataSetChanged()
        }
    }

    private fun onSavedSongSelected(savedSong: SavedSong) {
        val ft = activity?.supportFragmentManager?.beginTransaction()
        val fragment = SavedSongFragment.newInstance(savedSong)
        ft?.replace(R.id.fragment, fragment)?.addToBackStack("savedSong")
        ft?.commit()
    }


    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }


}