package com.cazapp.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.dialogs.AddToListDialog
import com.cazapp.dialogs.InputTextDialog
import com.cazapp.localdb.*
import com.cazapp.model.Song
import com.cazapp.repository.SongRepositoryFactory
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_song.*
import org.bson.types.ObjectId

class SongFragment : Fragment(), InputTextDialog.DialogListener {

    private lateinit var song: Song
    private var songViews = mutableListOf<View>()
    private var semitones = 0
    private var textSize = 15f
    private lateinit var db: LocalDataBase
    private lateinit var listLocalSongDao: ListLocalSongDao
    private lateinit var localSongDao: LocalSongDao
    private lateinit var savedSongDao: SavedSongDao
    private lateinit var diag: AddToListDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        db = SongRepositoryFactory.createLocalRepository(activity!!)
        listLocalSongDao = db.listLocalSongDao()
        localSongDao = db.localSongDao()
        savedSongDao = db.savedSongDao()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val songJson = arguments?.getSerializable(EXTRA_SONG_ID) as String
        song = Gson().fromJson(songJson, Song::class.java)
        drawSong()

        if (savedInstanceState != null) {
            semitones = savedInstanceState.getInt("semitones")
        }
        song.transpose(semitones)
        drawSong()

        (activity as MainActivity).actionbar.title = song.title

        seek_bar_song.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSize = progress.toFloat()
                drawSong()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                return
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                return
            }
        })

        close_change_size_layout_song.setOnClickListener {
            change_size_layout_song.visibility = View.GONE
        }



    }

    private fun drawSong() {
        song_layout.removeAllViews()
        for (line in song.body) {
            val lineView = TextView(activity!!)

            lineView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            lineView.text = line.content

            when (line.type) {
                "normal" -> {
                    lineView.typeface = Typeface.MONOSPACE
                    lineView.setTextColor(Color.parseColor("#2c2c2c"))
                }
                "estribillo" -> {
                    lineView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
                    lineView.setTextColor(Color.parseColor("#002183"))
                }
                "acorde" -> {
                    lineView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
                    lineView.setTextColor(Color.parseColor("#949494"))
                }
            }

            song_layout.addView(lineView)
            songViews.add(lineView)
        }
    }

    companion object {
        val EXTRA_SONG_ID = "com.cazapp.songid"
        fun newInstance(song: Song): SongFragment {
            val fragment = SongFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_SONG_ID, Gson().toJson(song,Song::class.java))
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?,inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_song,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_up_semitone -> {
            semitones++
            Log.d("Semitones", semitones.toString())
            song.transpose(1)
            drawSong()
            true
        }
        R.id.action_down_semitone -> {
            semitones--
            Log.d("Semitones", semitones.toString())
            song.transpose(-1)
            drawSong()
            true
        }
        R.id.action_add_list -> {

            diag = AddToListDialog()
            diag.setTargetFragment(this,0)
            val args = Bundle()
            args.putString("songId", song._id?.toHexString())
            args.putString("songTitle", song.title)
            args.putInt("semitones", semitones)
            args.putFloat("textSize", textSize)
            args.putString("songJson",Gson().toJson(song))
            diag.arguments = args
            diag.show((activity as MainActivity).supportFragmentManager, "AddToListDialog")



            true
        }
        R.id.action_change_size_song -> {
            change_size_layout_song.visibility = View.VISIBLE
            true
        }
        R.id.action_favorite -> {
            val savedSong = SavedSong.toSavedSong(song)
            if (savedSongDao.get(savedSong._id)==null) {
                savedSongDao.insert(savedSong)
            }else{
                savedSong.favorite = true
                savedSongDao.update(savedSong)
            }
            Snackbar.make(song_frame_layout,"Song added to favorites",Snackbar.LENGTH_SHORT).show()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }

    }

    override fun onDialogNegativeClick(dialog: InputTextDialog) {
        return
    }

    override fun onDialogPositiveClick(dialog: InputTextDialog) {

            val input = dialog.input
            val m_Text = input.text.toString()

            if (m_Text != "") {

                if ("." in m_Text) {
                    input.error = "title cannot contain '.'"
                    return
                }

                input.error = null

                val newList = ListLocalSong(ObjectId().toHexString(), m_Text)
                listLocalSongDao.insert(newList)

                dialog.dismiss()
                diag.updateList()


            } else {
                input.error = "Title cannot be empty"

            }

        }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("semitones", semitones)
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}
