package com.cazapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.adapters.ListSongAdapter
import com.cazapp.dialogs.InputTextDialog
import com.cazapp.localdb.ListLocalSong
import com.cazapp.localdb.ListLocalSongDao
import com.cazapp.localdb.LocalDataBase
import com.cazapp.localdb.LocalSongDao
import com.cazapp.repository.SongRepositoryFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_local_lists.*
import org.bson.types.ObjectId


class LocalListsFragment : Fragment(), InputTextDialog.DialogListener {

    lateinit var listLocalSongDao: ListLocalSongDao
    private lateinit var localSongDao: LocalSongDao
    var m_Text: String = ""
    lateinit var db: LocalDataBase

    val listener = object : ListSongAdapter.ListenerListLocalSong {

        override fun listenerSelect(listLocalSong: ListLocalSong) {
            val ft = activity?.supportFragmentManager?.beginTransaction()
            val fragment = LocalListFragment.newInstance(listLocalSong)
            ft?.replace(R.id.fragment, fragment)?.addToBackStack("list")
            ft?.commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d("CicloVida", "onCreate")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        Log.d("CicloVida", "onCreateView")
        return inflater.inflate(R.layout.fragment_local_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = SongRepositoryFactory.createLocalRepository(activity!!)
        localSongDao = db.localSongDao()
        listLocalSongDao = db.listLocalSongDao()
        (activity as MainActivity).actionbar.title = "Lists"
        //(activity as MainActivity).toolbar.setNavigationIcon(R.drawable.navigation_arrow)

        // Recycler view
        lists_list.apply {
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    context,
                    (layoutManager as androidx.recyclerview.widget.LinearLayoutManager).orientation
                )
            )
            update(listener)
        }

        create_list.setOnClickListener {
            val diag = InputTextDialog()
            diag.setTargetFragment(this,0)
            diag.show((activity as MainActivity).supportFragmentManager, "InputTextDialog")
        }
    }

    override fun onResume() {
        super.onResume()
        lists_list.update(listener)
    }


    fun androidx.recyclerview.widget.RecyclerView.update(listener: ListSongAdapter.ListenerListLocalSong) {
        val lists = listLocalSongDao.getAll().toMutableList()
        if (adapter == null) {
            adapter = ListSongAdapter(lists,listener)
        } else {
            (adapter as ListSongAdapter).songList = lists
            (adapter as ListSongAdapter).notifyDataSetChanged()
        }
    }

    private fun createNewList(title: String): ListLocalSong {
        val newList = ListLocalSong(ObjectId().toHexString(), title)
        listLocalSongDao.insert(newList)
        return newList
    }

    override fun onDialogNegativeClick(dialog: InputTextDialog) {
        return
    }

    override fun onDialogPositiveClick(dialog: InputTextDialog) {
        val input = dialog.input
        m_Text = input.text.toString()

        if (m_Text != "") {

            if ("." in m_Text) {
                input.error = "title cannot contain '.'"
                return
            }

            input.error = null

            createNewList(m_Text)
            lists_list.update(listener)

            dialog.dismiss()
            Snackbar.make(lists_list, "New list created", Snackbar.LENGTH_SHORT).show()
        } else {
            input.error = "Title cannot be empty"

        }
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}
