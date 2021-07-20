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
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.viewmodel.SongViewModel
import com.viet.myaudioplayer.adapter.ListSongSearchAdapter
import com.viet.myaudioplayer.viewmodel.SongOnlineViewModel
import kotlinx.android.synthetic.main.fragment_search.view.*

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)

        thread.start()
        handler = Handler(thread.looper)
        runnable = Runnable {
            this.activity?.runOnUiThread {
                view.editTextSearch.isEnabled = true
            }
        }

        listSongSearchAdapter =
            ListSongSearchAdapter(requireContext(), songViewModel)
        view.recyclerViewSearch.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recyclerViewSearch.adapter = listSongSearchAdapter

        view.editTextSearch.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                view.editTextSearch.isEnabled = false
                handler.postDelayed(runnable, 1000)
                search(view, view.editTextSearch.text.toString())
            }
            false
        }

        songViewModel.getAllSong().observe(viewLifecycleOwner, Observer {
            listSongSearchAdapter.notifyDataSetChanged()
        })

        return view
    }

    private fun search(view: View, text: String) {
        songOnlineViewModel.getListSearchObserver().observe(viewLifecycleOwner, Observer {
            view.progressBarLoadingSearch.visibility = View.GONE
            listSongSearchAdapter.setListSong(it)
        })
        songOnlineViewModel.searchSong(text)
    }
}