package com.cazapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.adapters.PublicSongAdapter
import com.cazapp.localdb.ListLocalSongDao
import com.cazapp.localdb.LocalSongDao
import com.cazapp.localdb.SavedSong
import com.cazapp.localdb.SavedSongDao
import com.cazapp.model.PublicList
import com.cazapp.model.PublicSong
import com.cazapp.model.Song
import com.cazapp.repository.SongRepository
import com.cazapp.repository.SongRepositoryFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_public_list.*


class PublicListFragment : Fragment() {

    private lateinit var publicList: PublicList

    private lateinit var listLocalSongDao: ListLocalSongDao
    private lateinit var localSongDao: LocalSongDao
    private lateinit var savedSongDao: SavedSongDao

    companion object {
        val EXTRA_PUBLIC_LIST_SONG_ID = "com.cazapp.publiclistid"
        fun newInstance(publicList: PublicList): PublicListFragment {
            val fragment = PublicListFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_PUBLIC_LIST_SONG_ID, publicList.toStringParse())
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_public_list,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.import_list -> {
            val title = "Import list?"
            val description = "Do you want to import the list to local lists?"
            val alert = "Imported list"

            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle(title)
            alertDialog.setMessage(description)
            alertDialog.setPositiveButton("OK") { _, _ ->
                val localList = publicList.toLocalList(true)
                val localSongs = publicList.toLocalSong(localList.id)
                val callback = object:SongRepository.ResultCallback<List<Song>>{
                    override fun onFailureExecute() {
                        return
                    }

                    override fun onPreExecute() {
                        return
                    }

                    override fun onSuccessExecute(result: List<Song>) {
                        savedSongDao.insertAll(result.map{ SavedSong.toSavedSong(it)})
                        listLocalSongDao.insert(localList)
                        localSongDao.insertAll(localSongs)

                        Snackbar.make(public_songs_list, alert, Snackbar.LENGTH_SHORT).show()
                    }
                }
                val idsSavedSongs = localSongs.filter { savedSongDao.get(it._id)==null }.map { it._id }
                (activity as MainActivity).repository!!.getSongsById(idsSavedSongs,callback)

            }
            alertDialog.setNegativeButton("CANCEL", null)
            alertDialog.show()
            true
        }
        else->{
            false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_public_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = SongRepositoryFactory.createLocalRepository(activity!!)
        localSongDao = db.localSongDao()
        listLocalSongDao = db.listLocalSongDao()
        savedSongDao = db.savedSongDao()

        // Get List
        val args = arguments?.getSerializable(EXTRA_PUBLIC_LIST_SONG_ID) as String
        publicList = PublicList.toPublicList(args)

        (activity as MainActivity).actionbar.title = publicList.title

        // Recycler view
        public_songs_list.apply {
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            update { publicSong -> onPublicSongSelected(publicSong) }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val itemImportList = menu.findItem(R.id.import_list)
        val visible = listLocalSongDao.get(publicList._id!!.toHexString())==null
        itemImportList.isVisible = visible
    }

    override fun onResume() {
        super.onResume()
        public_songs_list.update { publicSong -> onPublicSongSelected(publicSong) }
    }

    fun androidx.recyclerview.widget.RecyclerView.update(listener: (PublicSong) -> Unit) {
        if (adapter == null) {
            adapter = PublicSongAdapter(publicList.songs, listener)
        } else {
            (adapter as PublicSongAdapter).songList = publicList.songs
            (adapter as PublicSongAdapter).notifyDataSetChanged()
        }
    }

    private fun onPublicSongSelected(publicSong: PublicSong) {

        val callback = object : SongRepository.ResultCallback<Song> {
            override fun onFailureExecute() {
                loading_bar_public_list.visibility = View.GONE
                public_songs_list.visibility = View.VISIBLE

                Snackbar.make(public_songs_list, "Connection lost", Snackbar.LENGTH_LONG).show()
            }

            override fun onPreExecute() {
                // Animation of loading
                loading_bar_public_list.visibility = View.VISIBLE
                public_songs_list.visibility = View.GONE
                return
            }

            override fun onSuccessExecute(result: Song) {

                val ft = activity?.supportFragmentManager?.beginTransaction()
                val fragment = PublicSongFragment.newInstance(publicSong,result)
                ft?.replace(R.id.fragment, fragment)?.addToBackStack("publicSong")
                ft?.commit()

                loading_bar_public_list.visibility = View.GONE
                public_songs_list.visibility = View.VISIBLE

            }
        }

        //search song
        val repo = (activity as MainActivity).repository
        repo?.getSong(publicSong.idSong!!.toHexString(), callback)

    }
}