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
import com.viet.myaudioplayer.adapter.ListSongOnlineAdapter
import com.viet.myaudioplayer.viewmodel.SongOnlineViewModel
import kotlinx.android.synthetic.main.fragment_top_songs.view.*


class TopSongsFragment : Fragment() {

    private val songOnlineViewModel: SongOnlineViewModel by lazy {
        ViewModelProvider(
            this,
            SongOnlineViewModel.SongViewModelFactory(this.requireActivity().application)
        )[SongOnlineViewModel::class.java]
    }

    lateinit var listSongOnlineAdapter: ListSongOnlineAdapter

    private val songViewModel: SongViewModel by lazy {
        ViewModelProvider(
            this,
            SongViewModel.SongViewModelFactory(this.requireActivity().application)
        )[SongViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_top_songs, container, false)
        initRecyclerView(view)

        songViewModel.getAllSong().observe(viewLifecycleOwner, Observer {
            listSongOnlineAdapter.notifyDataSetChanged()
        })

        return view
    }

    private fun initRecyclerView(view: View) {
        listSongOnlineAdapter = ListSongOnlineAdapter(requireContext(), songViewModel)
        view.recyclerViewListTop.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recyclerViewListTop.adapter = listSongOnlineAdapter

        songOnlineViewModel.getListSongObserver().observe(viewLifecycleOwner, Observer {
            view.progressBarLoading.visibility = View.GONE
            listSongOnlineAdapter.setListSong(it)
        })
        songOnlineViewModel.getTopSong()
    }

}
