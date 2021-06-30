package com.viet.myaudioplayer.activity

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.adapter.MusicAdapter
import com.viet.myaudioplayer.model.MusicFiles
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_offline.*

class OfflineActivity : AppCompatActivity() {

    lateinit var musicAdapter: MusicAdapter

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline)

        MainActivity.musicFiles = getAllAudio(this)

        recyclerView.setHasFixedSize(true)
        if (MainActivity.musicFiles.size >= 1) {
            musicAdapter = MusicAdapter(
                this,
                MainActivity.musicFiles
            )
            recyclerView.adapter = musicAdapter
            recyclerView.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAllAudio(context: Context): MutableList<MusicFiles> {
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
        return tempAudioList
    }
}