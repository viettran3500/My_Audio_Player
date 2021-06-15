package com.viet.myaudioplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE = 1
    lateinit var viewPagerAdapter: ViewPagerAdapter
    companion object{
        var musicFiles: MutableList<MusicFiles> = mutableListOf()
        var albums: MutableList<MusicFiles> = mutableListOf()
        var shuffleBoolean = false
        var repeatBoolean = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        checkkPermission()
    }

    private fun checkkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE)
        }else{
            Toast.makeText(this@MainActivity,"Permission Granted", Toast.LENGTH_SHORT)
            musicFiles = getAllAudio(this)
            initViewPager()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                musicFiles = getAllAudio(this)
                initViewPager()
            }else{
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE)
            }
        }
    }

    fun initViewPager() {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragments(SongsFragment(), "Songs")
        viewPagerAdapter.addFragments(AlbumFragment(), "Albums")
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    fun getAllAudio(context: Context): MutableList<MusicFiles>{
        var duplicate: MutableList<String> = mutableListOf()

        var tempAudioList: MutableList<MusicFiles> = mutableListOf()
        var uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var projection = arrayOf(MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST)
        var cursor: Cursor? = context.contentResolver.query(uri,projection,null,null,null)
        if(cursor!=null){
            while (cursor.moveToNext()){
                var album = cursor.getString(0)
                var title = cursor.getString(1)
                var duration = cursor.getString(2)
                var path = cursor.getString(3)
                var artist = cursor.getString(4)

                var musicfile = MusicFiles(path,title,artist,album,duration)
                tempAudioList.add(musicfile)
                if(!duplicate.contains(album)){
                    albums.add(musicfile)
                    duplicate.add(album)
                }
            }
            cursor.close()
        }
        return tempAudioList
    }
}