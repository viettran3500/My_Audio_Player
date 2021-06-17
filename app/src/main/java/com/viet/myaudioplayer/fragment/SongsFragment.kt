package com.viet.myaudioplayer.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.activity.MainActivity
import com.viet.myaudioplayer.adapter.MusicAdapter
import kotlinx.android.synthetic.main.fragment_songs.view.*

class SongsFragment : Fragment() {

    lateinit var musicAdapter: MusicAdapter

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_songs, container, false)

        view.recyclerView.setHasFixedSize(true)
        if (MainActivity.musicFiles.size >= 1) {
            musicAdapter = MusicAdapter(
                this.context!!,
                MainActivity.musicFiles
            )
            view.recyclerView.adapter = musicAdapter
            view.recyclerView.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        return view
    }
}