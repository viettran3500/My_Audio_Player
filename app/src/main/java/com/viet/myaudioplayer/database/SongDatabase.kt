package com.viet.myaudioplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.viet.myaudioplayer.model.SongInfo

@Database(entities = [SongInfo::class], version = 1)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        var instance: SongDatabase? = null
        val DATABASE_NAME = "song.db"

        fun getInstance(context: Context): SongDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, SongDatabase::class.java, DATABASE_NAME)
                    .build()
            }
            return instance!!
        }

    }

}