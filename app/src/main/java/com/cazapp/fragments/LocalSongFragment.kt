package com.cazapp.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.TypedValue
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import androidx.room.Room
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.localdb.*
import com.cazapp.model.Song
import com.cazapp.repository.SongRepositoryFactory
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_local_song.*


class LocalSongFragment : Fragment() {

    lateinit var localSong: LocalSong
    lateinit var song: Song
    private lateinit var localSongDao: LocalSongDao
    private lateinit var listLocalSongDao : ListLocalSongDao
    private lateinit var db: LocalDataBase

    private var textSize = 15f
    private var semitones = 0

    companion object {
        val EXTRA_LOCAL_SONG_ID = "com.cazapp.localsongid"
        val EXTRA_SONG_ID = "com.cazapp.songid"
        fun newInstance(localSong: LocalSong, song: Song): LocalSongFragment {
            val fragment = LocalSongFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_LOCAL_SONG_ID, localSong.toString())
            bundle.putSerializable(EXTRA_SONG_ID, Gson().toJson(song, Song::class.java))
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        db = SongRepositoryFactory.createLocalRepository(activity!!)
        localSongDao = db.localSongDao()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_local_song, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_local_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // create publicSong from bundle
        val localSongString = arguments?.getSerializable(EXTRA_LOCAL_SONG_ID) as String
        val songString = arguments?.getSerializable(EXTRA_SONG_ID) as String

        localSong = LocalSong.toLocalSong(localSongString)
        listLocalSongDao = db.listLocalSongDao()
        textSize = localSong.textSize
        semitones = localSong.semitones

        song = Gson().fromJson(songString, Song::class.java)
        song.transpose(localSong.semitones)
        drawSong()

        (activity as MainActivity).actionbar.title = localSong.alt_title

        seek_bar_local_song.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
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

        close_change_size_layout.setOnClickListener {
            change_size_layout.animate()
                .translationY(close_change_size_layout.height.toFloat())
                .setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        change_size_layout.visibility = View.GONE
                    }
                })
        }
    }

    private fun drawSong() {
        local_song_layout.removeAllViews()
        for (line in song.body) {
            val lineView = TextView(activity as MainActivity)

            lineView.text = line.content
            lineView.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize)
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

            local_song_layout.addView(lineView)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_up_local_semitone -> {
            localSong.semitones++
            song.transpose(1)
            drawSong()
            true
        }
        R.id.action_down_local_semitone -> {
            localSong.semitones--
            song.transpose(-1)
            drawSong()
            true
        }
        R.id.action_delete_local_song -> {

            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle("Delete song from list?")
            alertDialog.setMessage("All song settings will be deleted")

            alertDialog.setPositiveButton("OK") { _, _ ->
                localSongDao.delete(localSong)
                activity?.supportFragmentManager?.popBackStack()
            }
            alertDialog.setNegativeButton("CANCEL", null)

            alertDialog.show()

            true
        }
        R.id.action_change_size -> {
            change_size_layout.visibility = View.VISIBLE
            change_size_layout.translationY = change_size_layout.height.toFloat()

            change_size_layout.animate()
                .translationY(0f)
                .setListener(null)

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        saveChanges()
        super.onPause()
    }

    private fun saveChanges(){
        localSong.textSize = textSize
        localSongDao.update(localSong)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        arguments?.putSerializable(EXTRA_LOCAL_SONG_ID, localSong.toString())
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}