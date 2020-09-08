package com.cazapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.adapters.ListSongAdapter
import com.cazapp.adapters.PublicListSongAdapter
import com.cazapp.dialogs.InputTextDialog
import com.cazapp.localdb.ListLocalSong
import com.cazapp.localdb.ListLocalSongDao
import com.cazapp.localdb.LocalDataBase
import com.cazapp.localdb.LocalSongDao
import com.cazapp.model.PublicList
import com.cazapp.repository.SongRepository
import com.cazapp.repository.SongRepositoryFactory
import kotlinx.android.synthetic.main.fragment_local_lists.*
import kotlinx.android.synthetic.main.fragment_public_lists.*
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.android.synthetic.main.list_holder.*
import org.bson.types.ObjectId


class PublicListsFragment : Fragment() {

    var m_Text: String = ""
    lateinit var db : LocalDataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_public_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = SongRepositoryFactory.createLocalRepository(activity!!)
        (activity as MainActivity).actionbar.title = "Public Lists"
        //(activity as MainActivity).toolbar.setNavigationIcon(R.drawable.navigation_arrow)

        // Recycler view
        public_lists_list.apply {
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    context,
                    (layoutManager as androidx.recyclerview.widget.LinearLayoutManager).orientation
                )
            )
            update { list -> onPublicListSelected(list) }
        }
    }

    override fun onResume() {
        super.onResume()
        public_lists_list.update { list -> onPublicListSelected(list) }
    }


    fun androidx.recyclerview.widget.RecyclerView.update(listener: (PublicList) -> Unit) {
        val callback = object : SongRepository.ResultCallback<List<PublicList>> {
            override fun onFailureExecute() {
                loading_bar_public_lists.visibility = View.GONE
                public_lists_list.visibility = View.VISIBLE
                Snackbar.make(layout_public_lists,"Connection error.",Snackbar.LENGTH_LONG).show()
                return
            }

            override fun onPreExecute() {
                loading_bar_public_lists.visibility = View.VISIBLE
                public_lists_list.visibility = View.GONE
                return
            }

            override fun onSuccessExecute(result: List<PublicList>) {
                if (adapter == null) {
                    adapter = PublicListSongAdapter(result, listener)
                } else {
                    (adapter as PublicListSongAdapter).publicLists = result
                    (adapter as PublicListSongAdapter).notifyDataSetChanged()
                }
                loading_bar_public_lists.visibility = View.GONE
                public_lists_list.visibility = View.VISIBLE
            }
        }
        (activity as MainActivity).repository?.getLists(callback)

    }

    private fun onPublicListSelected(publicList: PublicList) {

        val ft = activity?.supportFragmentManager?.beginTransaction()
        val fragment = PublicListFragment.newInstance(publicList)
        ft?.replace(R.id.fragment, fragment)?.addToBackStack("publicList")
        ft?.commit()
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}
