package com.viet.myaudioplayer.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.viet.myaudioplayer.database.SongRepository
import com.viet.myaudioplayer.model.SongInfo
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class SongViewModel(application: Application): ViewModel() {
    private val songRepository: SongRepository = SongRepository(application)

    fun insertSong(songInfo: SongInfo) = viewModelScope.launch {
        songRepository.insertSong(songInfo)
    }

    fun deleteSong(songInfo: SongInfo) = viewModelScope.launch {
        songRepository.deleteSong(songInfo)
    }

    fun getAllSong(): LiveData<List<SongInfo>> = songRepository.getAllSong()
    fun getSong(id: String): LiveData<SongInfo>? = songRepository.getSong(id)

    class SongViewModelFactory(val application: Application): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            if(modelClass.isAssignableFrom(SongViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return SongViewModel(application) as T
            }

            throw IllegalArgumentException("Unable construct viewModel")
        }

    }
}