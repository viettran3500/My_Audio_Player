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
import com.viet.myaudioplayer.viewmodel.SongViewModel
import com.viet.myaudioplayer.activity.MainActivity
import com.viet.myaudioplayer.adapter.ListSongFavoriteAdapter
import com.viet.myaudioplayer.databinding.FragmentFavoriteSongBinding
import com.viet.myaudioplayer.model.SongInfo

class FavoriteSongFragment : Fragment() {

    private val songViewModel: SongViewModel by lazy {
        ViewModelProvider(
            this,
            SongViewModel.SongViewModelFactory(this.requireActivity().application)
        )[SongViewModel::class.java]
    }

    lateinit var listSongFavoriteAdapter: ListSongFavoriteAdapter

    private lateinit var binding: FragmentFavoriteSongBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteSongBinding.inflate(layoutInflater, container, false)

        initRecyclerView()

        return binding.root
    }

    private fun initRecyclerView() {
        listSongFavoriteAdapter = ListSongFavoriteAdapter(requireContext(), songViewModel)
        binding.recyclerViewListFavorite.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerViewListFavorite.adapter = listSongFavoriteAdapter

        songViewModel.getAllSong().observe(viewLifecycleOwner, Observer {
            MainActivity.listMusicFavorite = it as MutableList<SongInfo>
            listSongFavoriteAdapter.setListSong(it)
        })
    }

}