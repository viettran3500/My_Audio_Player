package com.viet.myaudioplayer

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_album_details.*

class AlbumDetails : AppCompatActivity() {

    var albumName: String = ""
    lateinit var albumDetailAdapter: AlbumDetailAdapter

    companion object{
        var albumSongs: MutableList<MusicFiles> = mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_details)

        albumName = intent.getStringExtra("albumName").toString()
        albumSongs.clear()

        for (i in 0 until MainActivity.musicFiles.size){
            if(albumName == MainActivity.musicFiles[i].album){
                albumSongs.add(MainActivity.musicFiles[i])
            }
        }

        var image: ByteArray? = getAlbumArt(albumSongs[0].path)
        if(image != null){
            imgAlbumPhoto.setImageBitmap(BitmapFactory.decodeByteArray(image,0,image.size))
        }
        else{
            imgAlbumPhoto.setImageResource(R.drawable.music)
        }
    }

    override fun onResume() {
        super.onResume()
        if(albumSongs.size >= 1){
            albumDetailAdapter = AlbumDetailAdapter(this@AlbumDetails, albumSongs)
            recyclerView.adapter = albumDetailAdapter
            recyclerView.layoutManager = LinearLayoutManager(this@AlbumDetails, RecyclerView.VERTICAL, false)
        }
    }

    fun getAlbumArt(uri: String): ByteArray? {
        var retriever: MediaMetadataRetriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        var art: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return art
    }
}