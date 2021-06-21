package com.viet.myaudioplayer.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.adapter.AlbumDetailAdapter
import com.viet.myaudioplayer.model.MusicFiles
import kotlinx.android.synthetic.main.activity_album_details.*
import kotlinx.android.synthetic.main.activity_player.*
import java.lang.Exception

class AlbumDetails : AppCompatActivity() {

    var albumName: String = ""
    lateinit var albumDetailAdapter: AlbumDetailAdapter

    companion object {
        var albumSongs: MutableList<MusicFiles> = mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_details)

        albumName = intent.getStringExtra("albumName").toString()
        albumSongs.clear()

        for (i in 0 until MainActivity.musicFiles.size) {
            if (albumName == MainActivity.musicFiles[i].album) {
                albumSongs.add(MainActivity.musicFiles[i])
            }
        }

        val image: Bitmap? = getAlbumArt(albumSongs[0].path)
        if (image != null) {
            imgAlbumPhoto.setImageBitmap(image)
        } else {
            imgAlbumPhoto.setImageResource(R.drawable.music)
        }
    }

    override fun onResume() {
        super.onResume()
        if (albumSongs.size >= 1) {
            albumDetailAdapter =
                AlbumDetailAdapter(
                    this@AlbumDetails,
                    albumSongs
                )
            recyclerView.adapter = albumDetailAdapter
            recyclerView.layoutManager =
                LinearLayoutManager(this@AlbumDetails, RecyclerView.VERTICAL, false)
        }
    }

    private fun getAlbumArt(uri: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, Uri.parse(uri))
        val art: ByteArray? = retriever.embeddedPicture
        return if (art != null) {
            BitmapFactory.decodeByteArray(art, 0, art.size)
        } else {
            null
        }

    }
}