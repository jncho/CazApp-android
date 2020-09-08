package com.cazapp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.adapters.LocalSongAdapter
import com.cazapp.adapters.TouchHelperCallback
import com.cazapp.localdb.*
import com.cazapp.model.PublicList
import com.cazapp.model.Song
import com.cazapp.repository.SongRepository
import com.cazapp.repository.SongRepositoryFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_local_list.*
import java.io.File
import java.io.FileWriter


class LocalListFragment : Fragment(), LocalSongAdapter.OnStartDragListener {

    private lateinit var listSong: ListLocalSong
    private lateinit var localSongDao: LocalSongDao
    private lateinit var listLocalSongDao: ListLocalSongDao
    private lateinit var savedSongDao: SavedSongDao
    private lateinit var db: LocalDataBase

    private lateinit var touchHelper: ItemTouchHelper

    companion object {
        val EXTRA_LIST_SONG_ID = "com.cazapp.listid"
        fun newInstance(listLocalSong: ListLocalSong): LocalListFragment {
            val fragment = LocalListFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_LIST_SONG_ID, listLocalSong.toStringParse())
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_local_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get List
        db = SongRepositoryFactory.createLocalRepository(activity!!)
        localSongDao = db.localSongDao()
        listLocalSongDao = db.listLocalSongDao()
        savedSongDao = db.savedSongDao()
        val args = arguments?.getSerializable(EXTRA_LIST_SONG_ID) as String
        listSong = ListLocalSong.toListLocalSong(args)

        (activity as MainActivity).actionbar.title = listSong.title
        val listenerDrag = this

        // Recycler view
        local_songs_list.apply {
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            update(listenerDrag) { localSong -> onLocalSongSelected(localSong) }
            val callback = TouchHelperCallback(adapter as TouchHelperCallback.ItemTouchHelperAdapter)
            touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(this)
        }
    }

    override fun onResume() {
        super.onResume()
        local_songs_list.update(this) { localSong -> onLocalSongSelected(localSong) }
    }

    fun androidx.recyclerview.widget.RecyclerView.update(
        listenerDrag: LocalSongAdapter.OnStartDragListener,
        listener: (LocalSong) -> Unit
    ) {
        val songs = localSongDao.getAll(listSong.id).toMutableList()
        if (adapter == null) {
            adapter = LocalSongAdapter(songs, localSongDao, listenerDrag, listener)
        } else {
            (adapter as LocalSongAdapter).songList = songs
            (adapter as LocalSongAdapter).notifyDataSetChanged()
        }
    }

    override fun onStartDrag(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {

        R.id.delete_list -> {

            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle("Delete list?")
            alertDialog.setMessage("All the songs in this list will be deleted")

            alertDialog.setPositiveButton("OK") { _, _ ->
                val callback = object:SongRepository.ResultCallback<Boolean>{
                    override fun onFailureExecute() {
                        Snackbar.make(local_songs_list, "Reconnect the device to the network to delete the list.", Snackbar.LENGTH_LONG).show()
                    }

                    override fun onPreExecute() {

                    }

                    override fun onSuccessExecute(result: Boolean) {

                        val localSongs = localSongDao.getAll(listSong.id).toMutableList()
                        listLocalSongDao.delete(listSong)
                        localSongDao.deleteAll(localSongs)
                        val savedSongs = localSongs.filter{localSongDao.getByIdSong(it._id)==null}.map{savedSongDao.get(it._id)!!}.filter{!it.favorite}
                        savedSongDao.deleteAll(savedSongs)

                        activity?.supportFragmentManager?.popBackStack()
                        Snackbar.make(local_songs_list, "List deleted", Snackbar.LENGTH_SHORT).show()
                    }
                }

                (activity as MainActivity).repository!!.deleteList(listSong.id,callback)

            }
            alertDialog.setNegativeButton("CANCEL", null)
            alertDialog.show()

            true
        }
        R.id.share_list -> {

            // shareFile()
            val publicList = PublicList.localListToPublicList(listSong,localSongDao.getAll(listSong.id))
            val frameLoading = activity!!.findViewById<View>(R.id.frame_loading)
            val callback = object:SongRepository.ResultCallback<Boolean>{
                override fun onFailureExecute() {
                    frameLoading.visibility = View.GONE
                    Snackbar.make(drawer_layout,"Error to share list",Snackbar.LENGTH_LONG)
                }

                override fun onPreExecute() {
                    frameLoading.visibility = View.VISIBLE
                }

                override fun onSuccessExecute(result: Boolean) {
                    frameLoading.visibility = View.GONE
                    val url = "https://cazappsongs/${publicList._id}"
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            url
                        )
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
            }
            (activity as MainActivity).repository!!.createList(publicList,callback)

            true
        }
        R.id.publish_unpublish -> {
            // Comprobar si la lista está publicada o no
            val isLogged = (activity as MainActivity).repository!!.isLoggedIn()
            if (isLogged) {
                val callback = object : SongRepository.ResultCallback<PublicList?> {
                    override fun onFailureExecute() {
                        Snackbar.make(local_list_layout, "Connection lost", Snackbar.LENGTH_LONG).show()
                    }

                    override fun onPreExecute() {
                        return
                    }

                    override fun onSuccessExecute(result: PublicList?) {
                        // Crear un dialogo de aviso
                        if (result!=null) {
                            createUnpublishDialog(listSong).show()
                        } else {
                            createPublishDialog(listSong).show()
                        }
                        return
                    }
                }

                val repo = (activity as MainActivity).repository
                repo?.getList(listSong.id, callback)
            }else{
                (activity as MainActivity).loginDialog.show()
            }

            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    fun shareFile(){
        createFileList(listSong.title)

        val fileRead = File(context?.filesDir, "data/${listSong.title}.lca")
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(activity!!, "com.cazapp.fileprovider", fileRead)
            )
            type = "application/pdf"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun createPublishDialog(listLocalSong: ListLocalSong): AlertDialog.Builder {

        val title = "Publish list?"
        val description = "Everyone will be able to see this list. Are you sure?"
        val alert = "Lista publicada"
        val connectionError = "Connection Error"

        val alertDialog = AlertDialog.Builder(activity)
        alertDialog.setTitle(title)
        alertDialog.setMessage(description)
        alertDialog.setPositiveButton("Ok") { _, _ ->

            // Publish list
            val callback = object : SongRepository.ResultCallback<Boolean> {
                override fun onFailureExecute() {
                    Snackbar.make(local_list_layout, connectionError, Snackbar.LENGTH_SHORT).show()
                }

                override fun onPreExecute() {
                    return
                }

                override fun onSuccessExecute(result: Boolean) {
                    Snackbar.make(local_list_layout, alert, Snackbar.LENGTH_SHORT).show()
                }
            }
            val songs = localSongDao.getAll(listLocalSong.id).toMutableList()
            val repo = (activity as MainActivity).repository
            repo?.createList(PublicList.localListToPublicList(listLocalSong, songs), callback)
        }
        alertDialog.setNegativeButton("CANCEL",null)

        return alertDialog
    }

    fun createUnpublishDialog(listLocalSong: ListLocalSong): AlertDialog.Builder {

        val title = "Unpublish list?"
        val description = "The list will no longer be visible to everyone. Are you sure?"
        val alert = "Lista retirada"
        val connectionError = "Connection Error"

        val alertDialog = AlertDialog.Builder(activity)
        alertDialog.setTitle(title)
        alertDialog.setMessage(description)
        alertDialog.setPositiveButton("OK") { _, _ ->
            // Publish list
            val callback = object : SongRepository.ResultCallback<Boolean> {
                override fun onFailureExecute() {
                    Snackbar.make(local_list_layout, connectionError, Snackbar.LENGTH_SHORT).show()
                }

                override fun onPreExecute() {
                    return
                }

                override fun onSuccessExecute(result: Boolean) {
                    Snackbar.make(local_list_layout, alert, Snackbar.LENGTH_SHORT).show()
                }
            }
            val repo = (activity as MainActivity).repository
            repo?.deleteList(listLocalSong.id, callback)
        }
        alertDialog.setNegativeButton("CANCEL",null)

        return alertDialog
    }

    private fun createFileList(nameFile: String) {
        val file = File(context?.filesDir, "data")
        if (!file.exists()) {
            file.mkdir()
        }

        try {
            val gpxfile = File(file, "$nameFile.lca")
            val writer = FileWriter(gpxfile)

            var content = listSong.toStringParse()
            for (localSong in localSongDao.getAll(listSong.id)) {
                content += "\n" + localSong
            }

            writer.append(content)
            writer.flush()
            writer.close()

        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    private fun onLocalSongSelected(localSong: LocalSong) {
        val localSearch = true
        if (localSearch) {
            localSearchSong(localSong)
        } else {
            remoteSearchSong(localSong)
        }
    }

    private fun localSearchSong(localSong: LocalSong) {
        //Get savedsong
        val savedSong = savedSongDao.get(localSong._id)
        val ft = activity?.supportFragmentManager?.beginTransaction()
        val fragment = LocalSongFragment.newInstance(localSong, Song.toSong(savedSong!!))
        ft?.replace(R.id.fragment, fragment)?.addToBackStack("publicSong")
        ft?.commit()
    }

    private fun remoteSearchSong(localSong: LocalSong) {
        val callback = object : SongRepository.ResultCallback<Song> {
            override fun onFailureExecute() {
                loading_bar_list.visibility = View.GONE
                local_songs_list.visibility = View.VISIBLE

                Snackbar.make(local_songs_list, "Connection lost", Snackbar.LENGTH_LONG).show()
            }

            override fun onPreExecute() {
                // Animation of loading
                loading_bar_list.visibility = View.VISIBLE
                local_songs_list.visibility = View.GONE
                return
            }

            override fun onSuccessExecute(result: Song) {

                val ft = activity?.supportFragmentManager?.beginTransaction()
                val fragment = LocalSongFragment.newInstance(localSong, result)
                ft?.replace(R.id.fragment, fragment)?.addToBackStack("publicSong")
                ft?.commit()

                loading_bar_list.visibility = View.GONE
                local_songs_list.visibility = View.VISIBLE
            }
        }

        //search song
        val repo = (activity as MainActivity).repository
        repo?.getSong(localSong._id, callback)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveChanges()
    }

    private fun saveChanges() {
        if (local_songs_list != null) {
            (local_songs_list.adapter as LocalSongAdapter).updateSongs()
            listLocalSongDao.update(listSong)
        }
    }

    override fun onPause() {
        super.onPause()
        saveChanges()
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}