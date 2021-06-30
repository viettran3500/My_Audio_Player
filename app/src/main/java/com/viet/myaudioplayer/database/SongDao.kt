package com.viet.myaudioplayer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.viet.myaudioplayer.model.SongInfo

@Dao
interface SongDao {

    @Insert
    suspend fun insertSong(songInfo: SongInfo)

    @Delete
    suspend fun deleteSong(songInfo: SongInfo)

    @Query("SELECT * FROM song")
    fun getAllSong(): LiveData<List<SongInfo>>

    @Query("SELECT * FROM song WHERE id =:id")
    fun getSong(id: String): LiveData<SongInfo>?
}