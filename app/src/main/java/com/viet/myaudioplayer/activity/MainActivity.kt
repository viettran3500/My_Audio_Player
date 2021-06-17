package com.viet.myaudioplayer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.viet.myaudioplayer.fragment.AlbumFragment
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.fragment.SongsFragment
import com.viet.myaudioplayer.adapter.ViewPagerAdapter
import com.viet.myaudioplayer.model.MusicFiles
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE = 1
    lateinit var viewPagerAdapter: ViewPagerAdapter

    companion object {
        var musicFiles: MutableList<MusicFiles> = mutableListOf()
        var albums: MutableList<MusicFiles> = mutableListOf()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkkPermission()
    }

    @SuppressLint("ShowToast")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        } else {
            Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT)
            musicFiles = getAllAudio(this)
            initViewPager()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                musicFiles = getAllAudio(this)
                initViewPager()
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            }
        }
    }

    private fun initViewPager() {
        viewPagerAdapter =
            ViewPagerAdapter(this)
        viewPagerAdapter.addFragments(SongsFragment(), "Songs")
        viewPagerAdapter.addFragments(AlbumFragment(), "Albums")
        viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            tab.text = viewPagerAdapter.titles[position]
        }.attach()
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
                        albums.add(musicfile)
                        duplicate.add(album)
                    }
                }
            }
            cursor.close()
        }
        return tempAudioList
    }
}