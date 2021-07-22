package com.viet.myaudioplayer.viewmodel

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.viet.myaudioplayer.MusicService
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.model.SongInfo
import java.lang.IllegalArgumentException

class PlayViewModel : ViewModel() {

    private val isShuffle: MutableLiveData<Boolean> = MutableLiveData()
    val isRepeat: MutableLiveData<Int> = MutableLiveData()
    val isPlaying: MutableLiveData<Boolean> = MutableLiveData()

    fun setShuffle(isS: Boolean) {
        isShuffle.postValue(isS)
    }

    fun getShuffle() = isShuffle

    class SongViewModelFactory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(PlayViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlayViewModel() as T
            }

            throw IllegalArgumentException("Unable construct viewModel")
        }

    }
}