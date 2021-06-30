package com.viet.myaudioplayer.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.viewmodel.SongViewModel
import com.viet.myaudioplayer.activity.MainActivity
import com.viet.myaudioplayer.adapter.ListSongFavoriteAdapter
import com.viet.myaudioplayer.model.SongInfo
import kotlinx.android.synthetic.main.fragment_favorite_song.view.*

class FavoriteSongFragment : Fragment(){

    private val songViewModel: SongViewModel by lazy {
        ViewModelProvider(
            this,
            SongViewModel.SongViewModelFactory(this.requireActivity().application)
        )[SongViewModel::class.java]
    }

    lateinit var listSongFavoriteAdapter: ListSongFavoriteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_favorite_song, container, false)


        initRecyclerView(view)


        return view
    }

    private fun initRecyclerView(view: View) {
        listSongFavoriteAdapter = ListSongFavoriteAdapter(requireContext(), songViewModel)
        view.recyclerViewListFavorite.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recyclerViewListFavorite.adapter = listSongFavoriteAdapter

        songViewModel.getAllSong().observe(viewLifecycleOwner, Observer {
            MainActivity.listMusicFavorite = it as MutableList<SongInfo>
            listSongFavoriteAdapter.setListSong(it as MutableList<SongInfo>)
        })
    }

}