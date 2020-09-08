package com.cazapp.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.TypedValue
import android.view.*
import android.widget.SeekBar
import android.widget.TextView
import com.cazapp.R
import com.cazapp.activities.MainActivity
import com.cazapp.model.PublicSong
import com.cazapp.model.Song
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_local_song.*
import kotlinx.android.synthetic.main.fragment_public_song.*


class PublicSongFragment : Fragment() {

    lateinit var publicSong: PublicSong
    lateinit var song: Song

    private var textSize = 15f
    private var semitones = 0

    companion object {
        val EXTRA_PUBLIC_SONG_ID = "com.cazapp.publicsongid"
        val EXTRA_SONG_ID = "com.cazapp.songid"
        fun newInstance(publicSong: PublicSong, song: Song): PublicSongFragment {
            val fragment = PublicSongFragment()
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_PUBLIC_SONG_ID, publicSong.toStringParse())
            bundle.putSerializable(EXTRA_SONG_ID, Gson().toJson(song, Song::class.java))
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_public_song, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_public_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // create publicSong from bundle
        val publicSongString = arguments?.getSerializable(EXTRA_PUBLIC_SONG_ID) as String
        val songString = arguments?.getSerializable(EXTRA_SONG_ID) as String

        publicSong = PublicSong.toPublicSong(publicSongString)
        textSize = publicSong.textSize
        semitones = publicSong.semitones

        song = Gson().fromJson(songString, Song::class.java)
        song.transpose(publicSong.semitones)
        drawSong()

        (activity as MainActivity).actionbar.title = publicSong.alt_title

        seek_bar_public_song.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        close_change_size_public_layout.setOnClickListener {
            change_size_public_layout.animate()
                .translationY(close_change_size_public_layout.height.toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        change_size_public_layout.visibility = View.GONE
                    }
                })
        }
    }

    private fun drawSong() {
        public_song_layout.removeAllViews()
        for (line in song.body) {
            val lineView = TextView(activity as MainActivity)

            lineView.text = line.content
            lineView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
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

            public_song_layout.addView(lineView)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_up_public_semitone -> {
            publicSong.semitones++
            song.transpose(1)
            drawSong()
            true
        }
        R.id.action_down_public_semitone -> {
            publicSong.semitones--
            song.transpose(-1)
            drawSong()
            true
        }
        R.id.action_change_public_size -> {
            change_size_public_layout.visibility = View.VISIBLE
            change_size_public_layout.translationY = change_size_public_layout.height.toFloat()


            change_size_public_layout.animate()
                .translationY(0f)
                .setListener(null)

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        arguments?.putSerializable(EXTRA_PUBLIC_SONG_ID, publicSong.toStringParse())
    }

}