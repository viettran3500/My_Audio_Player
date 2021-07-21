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
import com.viet.myaudioplayer.adapter.ListSongOnlineAdapter
import com.viet.myaudioplayer.databinding.FragmentTopSongsBinding
import com.viet.myaudioplayer.viewmodel.SongOnlineViewModel


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

    private lateinit var binding: FragmentTopSongsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTopSongsBinding.inflate(layoutInflater, container, false)

        initRecyclerView()

        songViewModel.getAllSong().observe(viewLifecycleOwner, Observer {
            listSongOnlineAdapter.notifyDataSetChanged()
        })

        return binding.root
    }

    private fun initRecyclerView() {
        listSongOnlineAdapter = ListSongOnlineAdapter(requireContext(), songViewModel)
        binding.recyclerViewListTop.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerViewListTop.adapter = listSongOnlineAdapter

        songOnlineViewModel.getListSongObserver().observe(viewLifecycleOwner, Observer {
            binding.progressBarLoading.visibility = View.GONE
            listSongOnlineAdapter.setListSong(it)
        })
        songOnlineViewModel.getTopSong()
    }

}
