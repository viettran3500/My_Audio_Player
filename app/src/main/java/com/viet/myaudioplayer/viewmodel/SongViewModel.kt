package com.viet.myaudioplayer.viewmodel

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.viet.myaudioplayer.activity.MainActivity
import com.viet.myaudioplayer.database.SongRepository
import com.viet.myaudioplayer.model.MusicFiles
import com.viet.myaudioplayer.model.SongInfo
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class SongViewModel(application: Application) : ViewModel() {
    private val songRepository: SongRepository = SongRepository(application)
    private var songOffline: MutableLiveData<MutableList<MusicFiles>> = MutableLiveData()

    fun getSongOffObserver(): MutableLiveData<MutableList<MusicFiles>> {
        return songOffline
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadSongOff(context: Context) = viewModelScope.launch {
        val duplicate: MutableList<String> = mutableListOf()
        val tempAudioList: MutableList<MusicFiles> = mutableListOf()

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val album = cursor.getString(0)
                val title = cursor.getString(1)
                val duration = cursor.getString(2)
                var path = cursor.getString(3)
                path = ContentUris.withAppendedId(uri, path.toLong()).toString()
                val artist = cursor.getString(4)
                var albumId = cursor.getString(5)
                albumId =
                    ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId.toLong()
                    )
                        .toString()

                if (duration.toInt() > 30000) {
                    val musicfile = MusicFiles(
                        path,
                        title,
                        artist,
                        album,
                        albumId,
                        duration
                    )
                    tempAudioList.add(musicfile)
                    if (!duplicate.contains(album)) {
                        MainActivity.albums.add(musicfile)
                        duplicate.add(album)
                    }
                }
            }
            cursor.close()
        }
        songOffline.postValue(tempAudioList)
    }

    fun insertSong(songInfo: SongInfo) = viewModelScope.launch {
        songRepository.insertSong(songInfo)
    }

    fun deleteSong(songInfo: SongInfo) = viewModelScope.launch {
        songRepository.deleteSong(songInfo)
    }

    fun getAllSong() = songRepository.getAllSong()

    class SongViewModelFactory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SongViewModel(application) as T
            }

            throw IllegalArgumentException("Unable construct viewModel")
        }

    }
}