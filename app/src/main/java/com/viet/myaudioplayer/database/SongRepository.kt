package com.viet.myaudioplayer.database

import android.app.Application
import androidx.lifecycle.LiveData
import com.viet.myaudioplayer.model.SongInfo

class SongRepository(app: Application) {

    private val songDao: SongDao

    init {
        val songDatabase: SongDatabase = SongDatabase.getInstance(app)
        songDao = songDatabase.songDao()
    }

    suspend fun insertSong(songInfo: SongInfo) = songDao.insertSong(songInfo)
    suspend fun deleteSong(songInfo: SongInfo) = songDao.deleteSong(songInfo)
    fun getAllSong(): LiveData<List<SongInfo>> = songDao.getAllSong()
    fun getSong(id:String): LiveData<SongInfo>? = songDao.getSong(id)
}