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
import com.viet.myaudioplayer.adapter.ListSongOnlineAdapter
import com.viet.myaudioplayer.api.ApiService
import com.viet.myaudioplayer.model.SongInfo
import com.viet.myaudioplayer.model.infomusic.DataInfo
import com.viet.myaudioplayer.model.top.ItemSong
import com.viet.myaudioplayer.model.top.ItemTop
import kotlinx.android.synthetic.main.fragment_top_songs.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TopSongsFragment : Fragment() {


    var listTop: MutableList<ItemSong> = mutableListOf()
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

        listSongOnlineAdapter =
            ListSongOnlineAdapter(requireContext(), MainActivity.listMusicTop, songViewModel)
        view.recyclerViewListTop.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recyclerViewListTop.adapter = listSongOnlineAdapter

        loadTopSong(view)

        songViewModel.getAllSong().observe(viewLifecycleOwner, Observer {
            listSongOnlineAdapter.notifyDataSetChanged()
        })

        return view
    }

    private fun loadTopSong(view: View) {

        view.progressBarLoading.visibility = View.VISIBLE
        ApiService.apiService.getTopSong().enqueue(object : Callback<ItemTop> {
            override fun onFailure(call: Call<ItemTop>, t: Throwable) {
            }

            override fun onResponse(call: Call<ItemTop>, response: Response<ItemTop>) {
                var itemTop: ItemTop? = response.body()
                if (itemTop != null && itemTop.err == 0) {
                    listTop = itemTop.data.song as MutableList<ItemSong>
                    loadInfo(view)
                }
            }
        })
    }

    private fun loadInfo(view: View) {
        for (i in 0 until listTop.size) {
            MainActivity.listMusicTop.add(
                SongInfo(
                    listTop[i].id,
                    listTop[i].title,
                    listTop[i].artistsNames,
                    listTop[i].thumbnail,
                    listTop[i].duration,
                    null,
                    null
                )
            )
            ApiService.apiService.getInfoMusic(listTop[i].id).enqueue(object : Callback<DataInfo> {
                override fun onFailure(call: Call<DataInfo>, t: Throwable) {
                }

                override fun onResponse(call: Call<DataInfo>, response: Response<DataInfo>) {
                    val dataInfo: DataInfo? = response.body()
                    if (dataInfo != null && dataInfo.err == 0) {
                        var genre = ""
                        for (element in dataInfo.data.genres) {
                            genre += "${element.name} "
                        }
                        MainActivity.listMusicTop[i].genre = genre
                        MainActivity.listMusicTop[i].source =
                            "http://api.mp3.zing.vn/api/streaming/audio/${MainActivity.listMusicTop[i].id}/128"

                        listSongOnlineAdapter?.notifyDataSetChanged()
                        view.progressBarLoading.visibility = View.GONE
                    }
                }

            })
        }
    }

}
