package com.viet.myaudioplayer.fragment

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.viewmodel.SongViewModel
import com.viet.myaudioplayer.adapter.ListSongSearchAdapter
import com.viet.myaudioplayer.databinding.FragmentSearchBinding
import com.viet.myaudioplayer.viewmodel.SongOnlineViewModel

class SearchFragment : Fragment() {

    var thread: HandlerThread = HandlerThread("Thread")
    lateinit var runnable: Runnable
    lateinit var handler: Handler

    private val songOnlineViewModel: SongOnlineViewModel by lazy {
        ViewModelProvider(
            this,
            SongOnlineViewModel.SongViewModelFactory(this.requireActivity().application)
        )[SongOnlineViewModel::class.java]
    }

    lateinit var listSongSearchAdapter: ListSongSearchAdapter

    private val songViewModel: SongViewModel by lazy {
        ViewModelProvider(
            this,
            SongViewModel.SongViewModelFactory(this.requireActivity().application)
        )[SongViewModel::class.java]
    }

    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater, container, false)

        thread.start()
        handler = Handler(thread.looper)
        runnable = Runnable {
            this.activity?.runOnUiThread {
                binding.editTextSearch.isEnabled = true
            }
        }

        listSongSearchAdapter =
            ListSongSearchAdapter(requireContext(), songViewModel)
        binding.recyclerViewSearch.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerViewSearch.adapter = listSongSearchAdapter

        binding.editTextSearch.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                binding.editTextSearch.isEnabled = false
                handler.postDelayed(runnable, 1000)
                search(binding.editTextSearch.text.toString())
            }
            false
        }

        songViewModel.getAllSong().observe(viewLifecycleOwner, Observer {
            listSongSearchAdapter.notifyDataSetChanged()
        })

        return binding.root
    }

    private fun search(text: String) {
        songOnlineViewModel.getListSearchObserver().observe(viewLifecycleOwner, Observer {
            binding.progressBarLoadingSearch.visibility = View.GONE
            listSongSearchAdapter.setListSong(it)
        })
        songOnlineViewModel.searchSong(text)
    }
}