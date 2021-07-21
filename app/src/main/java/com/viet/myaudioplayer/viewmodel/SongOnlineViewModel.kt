package com.viet.myaudioplayer.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.viet.myaudioplayer.activity.MainActivity
import com.viet.myaudioplayer.api.ApiSearch
import com.viet.myaudioplayer.api.ApiService
import com.viet.myaudioplayer.model.SongInfo
import com.viet.myaudioplayer.model.infomusic.DataInfo
import com.viet.myaudioplayer.model.infomusic.InfoMusic
import com.viet.myaudioplayer.model.recommend.ItemRecommend
import com.viet.myaudioplayer.model.search.Root
import com.viet.myaudioplayer.model.search.Song
import com.viet.myaudioplayer.model.top.ItemSong
import com.viet.myaudioplayer.model.top.ItemTop
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException

class SongOnlineViewModel : ViewModel() {
    var listSongOnline: MutableLiveData<MutableList<SongInfo>> = MutableLiveData()
    var listSearchSong: MutableLiveData<MutableList<SongInfo>> = MutableLiveData()
    var listSongRelated: MutableLiveData<MutableList<SongInfo>> = MutableLiveData()
    var infoMusic: MutableLiveData<InfoMusic> = MutableLiveData()

    fun getInfoObserver(): MutableLiveData<InfoMusic> {
        return infoMusic
    }

    fun loadInfo(id: String) = viewModelScope.launch {

        ApiService.apiService.getInfoMusic(id).enqueue(object : Callback<DataInfo> {
            override fun onFailure(call: Call<DataInfo>, t: Throwable) {
                infoMusic.postValue(null)
            }

            override fun onResponse(call: Call<DataInfo>, response: Response<DataInfo>) {
                val dataInfo: DataInfo? = response.body()
                if (dataInfo != null && dataInfo.err == 0) {
                    infoMusic.postValue(dataInfo.data)
                } else {
                    infoMusic.postValue(null)
                }
            }

        })
    }

    fun getListSongObserver(): MutableLiveData<MutableList<SongInfo>> {
        return listSongOnline
    }

    fun getListSearchObserver(): MutableLiveData<MutableList<SongInfo>> {
        return listSearchSong
    }

    fun getListSongRelatedObserver(): MutableLiveData<MutableList<SongInfo>> {
        return listSongRelated
    }

    fun loadRelatedSong(id: String) = viewModelScope.launch {

        ApiService.apiService.getSongRelated(id)
            .enqueue(object : Callback<ItemRecommend> {
                override fun onFailure(call: Call<ItemRecommend>, t: Throwable) {
                    listSongRelated.postValue(null)
                }

                override fun onResponse(
                    call: Call<ItemRecommend>,
                    response: Response<ItemRecommend>
                ) {
                    MainActivity.listSongRelated.clear()
                    val item: ItemRecommend? = response.body()
                    if (item != null && item.err == 0) {
                        val list = item.data.items as MutableList<ItemSong>

                        for (i in 0 until list.size) {
                            MainActivity.listSongRelated.add(
                                SongInfo(
                                    list[i].id,
                                    list[i].name,
                                    list[i].artistsNames,
                                    list[i].thumbnail,
                                    list[i].duration,
                                    null,
                                    "http://api.mp3.zing.vn/api/streaming/audio/${list[i].id}/128"
                                )
                            )
                        }
                        listSongRelated.postValue(MainActivity.listSongRelated)
                    } else {
                        listSongRelated.postValue(null)
                    }
                }
            })
    }

    fun searchSong(text: String) = viewModelScope.launch {
        ApiSearch.apiSearch.searchSong(text).enqueue(object : Callback<Root> {
            override fun onFailure(call: Call<Root>, t: Throwable) {
                listSearchSong.postValue(null)
            }

            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                MainActivity.listMusicSearch.clear()
                val itemSearch: Root? = response.body()
                if (itemSearch != null && itemSearch.result) {
                    val listSearch = itemSearch.data!![0].song as MutableList<Song>

                    for (i in 0 until listSearch.size) {
                        MainActivity.listMusicSearch.add(
                            SongInfo(
                                listSearch[i].id!!,
                                listSearch[i].name!!,
                                listSearch[i].artist!!,
                                "https://photo-resize-zmp3.zadn.vn/${listSearch[i].thumb}",
                                listSearch[i].duration!!.toInt(),
                                null,
                                "http://api.mp3.zing.vn/api/streaming/audio/${listSearch[i].id}/128"
                            )
                        )
                    }
                    listSearchSong.postValue(MainActivity.listMusicSearch)
                } else {
                    listSearchSong.postValue(null)
                }
            }

        })
    }

    fun getTopSong() = viewModelScope.launch {
        ApiService.apiService.getTopSong().enqueue(object : Callback<ItemTop> {
            override fun onFailure(call: Call<ItemTop>, t: Throwable) {
                listSongOnline.postValue(null)
            }

            override fun onResponse(call: Call<ItemTop>, response: Response<ItemTop>) {
                val itemTop: ItemTop? = response.body()
                if (itemTop != null && itemTop.err == 0) {
                    val listTop = itemTop.data.song as MutableList<ItemSong>
                    for (i in 0 until listTop.size) {
                        MainActivity.listMusicTop.add(
                            SongInfo(
                                listTop[i].id,
                                listTop[i].title,
                                listTop[i].artistsNames,
                                listTop[i].thumbnail,
                                listTop[i].duration,
                                null,
                                "http://api.mp3.zing.vn/api/streaming/audio/${listTop[i].id}/128"
                            )
                        )
                    }
                    listSongOnline.postValue(MainActivity.listMusicTop)
                } else {
                    listSongOnline.postValue(null)
                }
            }
        })
    }

    class SongViewModelFactory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(SongOnlineViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SongOnlineViewModel() as T
            }

            throw IllegalArgumentException("Unable construct viewModel")
        }

    }
}