package com.viet.myaudioplayer.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.activity.MainActivity
import com.viet.myaudioplayer.adapter.AlbumAdapter
import kotlinx.android.synthetic.main.fragment_album.view.*

class AlbumFragment : Fragment() {

    lateinit var albumAdapter: AlbumAdapter

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_album, container, false)
        view.recyclerView.setHasFixedSize(true)
        if (MainActivity.albums.size >= 1) {
            albumAdapter = AlbumAdapter(
                this.context!!,
                MainActivity.albums
            )
            view.recyclerView.adapter = albumAdapter
            view.recyclerView.layoutManager = GridLayoutManager(this.context!!, 2)
        }

        return view
    }
}