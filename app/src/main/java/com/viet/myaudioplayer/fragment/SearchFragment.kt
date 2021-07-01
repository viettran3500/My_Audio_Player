package com.viet.myaudioplayer.fragment

import android.os.Bundle
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
import com.viet.myaudioplayer.activity.MainActivity
import com.viet.myaudioplayer.adapter.ListSongSearchAdapter
import com.viet.myaudioplayer.api.ApiSearch
import com.viet.myaudioplayer.api.ApiService
import com.viet.myaudioplayer.model.SongInfo
import com.viet.myaudioplayer.model.infomusic.DataInfo
import com.viet.myaudioplayer.model.search.Root
import com.viet.myaudioplayer.model.search.Song
import kotlinx.android.synthetic.main.fragment_search.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    var listSearch: MutableList<Song> = mutableListOf()
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

        listSongSearchAdapter =
            ListSongSearchAdapter(requireContext(), MainActivity.listMusicSearch, songViewModel)
        view.recyclerViewSearch.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recyclerViewSearch.adapter = listSongSearchAdapter

        view.editTextSearch.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                search(view, view.editTextSearch.text.toString())
            }
            false
        }

        songViewModel.getAllSong().observe(viewLifecycleOwner, Observer {
            listSongSearchAdapter.notifyDataSetChanged()
        })

        return view
    }

    fun search(view: View, text: String) {
        view.progressBarLoadingSearch.visibility = View.VISIBLE
        view.editTextSearch.isEnabled = false

        ApiSearch.apiSearch.searchSong(text).enqueue(object : Callback<Root> {
            override fun onFailure(call: Call<Root>, t: Throwable) {

            }

            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                val itemSearch: Root? = response.body()
                if (itemSearch != null && itemSearch.result) {
                    listSearch = itemSearch.data!![0].song as MutableList<Song>
                    getInfo(view)
                }
            }

        })
    }

    fun getInfo(view: View) {
        for (i in 0 until listSearch.size) {
            MainActivity.listMusicSearch.add(
                SongInfo(
                    listSearch[i].id!!,
                    listSearch[i].name!!,
                    listSearch[i].artist!!,
                    "https://photo-resize-zmp3.zadn.vn/${listSearch[i].thumb}",
                    listSearch[i].duration!!.toInt(),
                    null,
                    null
                )
            )

            ApiService.apiService.getInfoMusic(listSearch[i].id!!)
                .enqueue(object : Callback<DataInfo> {
                    override fun onFailure(call: Call<DataInfo>, t: Throwable) {

                    }

                    override fun onResponse(call: Call<DataInfo>, response: Response<DataInfo>) {
                        val dataInfo: DataInfo? = response.body()
                        if (dataInfo != null && dataInfo.err == 0 && dataInfo.msg == "Success") {

                            var genre = ""
                            for (element in dataInfo.data.genres) {
                                genre += "${element.name} "
                            }
                            MainActivity.listMusicSearch[i].genre = genre
                            MainActivity.listMusicSearch[i].source =
                                "http://api.mp3.zing.vn/api/streaming/audio/${MainActivity.listMusicSearch[i].id}/128"

                            listSongSearchAdapter.notifyDataSetChanged()

                            view.progressBarLoadingSearch.visibility = View.GONE
                            view.editTextSearch.isEnabled = true
                        }
                    }

                })
        }

    }
}