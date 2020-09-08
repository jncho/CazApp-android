package com.cazapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.View
import android.widget.*
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.fragments.SongFragment
import com.cazapp.localdb.*
import com.cazapp.model.Song
import com.cazapp.repository.SongRepositoryFactory
import com.google.gson.Gson
import java.lang.Exception

class AddToListDialog : DialogFragment() {

    lateinit var db: LocalDataBase
    lateinit var localSongDao: LocalSongDao
    lateinit var listLocalSongDao: ListLocalSongDao
    lateinit var savedSongDao: SavedSongDao
    lateinit var dialogView : View
    lateinit var lists: List<ListLocalSong>

    lateinit var idSong : String
    lateinit var title : String
    var semitones : Int = 0
    var textSize : Float = 0f
    lateinit var savedSong: SavedSong

    override fun onAttach(context: Context) {
        super.onAttach(context)
        db = SongRepositoryFactory.createLocalRepository(context)
        localSongDao = db.localSongDao()
        listLocalSongDao = db.listLocalSongDao()
        savedSongDao = db.savedSongDao()

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select list")
        dialogView = requireActivity().layoutInflater.inflate(R.layout.dialog_add_to_list, null)
        builder.setView(dialogView)
        builder.setPositiveButton("New List",null)
        builder.setNegativeButton("CANCEL",null)

        idSong =  arguments?.getString("songId")!!
        title = arguments?.getString("songTitle")!!
        semitones = arguments?.getInt("semitones")!!
        textSize = arguments?.getFloat("textSize")!!
        val songJson = arguments?.getString("songJson")!!
        val song = Gson().fromJson(songJson,Song::class.java)
        savedSong = SavedSong.toSavedSong(song)
        savedSong.favorite=false

        return builder.create()

    }

    fun updateList(){
        lists = listLocalSongDao.getAll()
        val adapter = ArrayAdapter<ListLocalSong>(context!!, R.layout.dialog_add_to_list_holder, lists)
        val listView = dialog.findViewById<ListView>(R.id.add_to_list_dialog)
        listView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()

        val alertDialog = dialog as AlertDialog
        val okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.setOnClickListener{
            val diag = InputTextDialog()
            diag.setTargetFragment(targetFragment,targetRequestCode)
            diag.show((activity as MainActivity).supportFragmentManager, "InputTextDialog")
        }

        updateList()

        val listView = dialog.findViewById<ListView>(R.id.add_to_list_dialog)
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val listSong = lists[position]
            val priority = localSongDao.getLastPriority(listSong.id)
            val localSong = LocalSong(idSong, listSong.id, title, title, semitones, priority + 1, textSize)
            try {
                if (savedSongDao.get(savedSong._id)==null){
                    savedSongDao.insert(savedSong)
                }
                localSongDao.insert(localSong)
                dialog.dismiss()
            }catch (e : SQLiteConstraintException){

                val builder = AlertDialog.Builder(activity)
                builder.setTitle("The song cannot be added")
                builder.setMessage("Song already exits in this list")
                builder.setPositiveButton("OK",null)
                builder.create().show()
            }


        }

    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}