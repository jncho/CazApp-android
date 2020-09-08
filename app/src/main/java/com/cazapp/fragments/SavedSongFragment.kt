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

class SavedSongFragment : Fragment() {

    private lateinit var song: Song
    private var songViews = mutableListOf<View>()
    private var semitones = 0
    private var textSize = 15f
    private lateinit var db: LocalDataBase
    private lateinit var savedSongDao: SavedSongDao
    private lateinit var diag: AddToListDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        db = SongRepositoryFactory.createLocalRepository(activity!!)
        savedSongDao = db.savedSongDao()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val songId = arguments?.getSerializable(EXTRA_SAVED_SONG_ID) as String
        song = Song.toSong(savedSongDao.get(songId)!!)
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
        val EXTRA_SAVED_SONG_ID = "com.cazapp.savedsongid"
        fun newInstance(song: SavedSong): SavedSongFragment {
            val fragment = SavedSongFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_SAVED_SONG_ID, song._id)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?,inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_saved_song,menu)
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

        R.id.action_change_size_song -> {
            change_size_layout_song.visibility = View.VISIBLE
            true
        }
        R.id.action_delete -> {
            savedSongDao.delete(SavedSong.toSavedSong(song))
            (activity as MainActivity).setFragment(3)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
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
