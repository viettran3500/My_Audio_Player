package com.viet.myaudioplayer.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.viewmodel.SongViewModel
import com.viet.myaudioplayer.adapter.ViewPagerAdapter
import com.viet.myaudioplayer.databinding.ActivityMainBinding
import com.viet.myaudioplayer.fragment.*
import com.viet.myaudioplayer.model.MusicFiles
import com.viet.myaudioplayer.model.SongInfo

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE = 1
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val topSongsFragment: TopSongsFragment = TopSongsFragment()
    private val searchFragment: SearchFragment = SearchFragment()
    private val favoriteSongFragment: FavoriteSongFragment = FavoriteSongFragment()

    companion object {
        var musicFiles: MutableList<MusicFiles> = mutableListOf()
        var albums: MutableList<MusicFiles> = mutableListOf()
        var listMusicTop: MutableList<SongInfo> = mutableListOf()
        var listMusicSearch: MutableList<SongInfo> = mutableListOf()
        var listMusicFavorite: MutableList<SongInfo> = mutableListOf()
        var listSongRelated: MutableList<SongInfo> = mutableListOf()
    }

    private val songViewModel: SongViewModel by lazy {
        ViewModelProvider(
            this,
            SongViewModel.SongViewModelFactory(this.application)
        )[SongViewModel::class.java]
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        checkkPermission()

        songViewModel.getAllSong().observe(this, Observer {
            listMusicFavorite = it as MutableList<SongInfo>
        })
    }

    @SuppressLint("ShowToast")
    private fun checkkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
        } else {
            Toast.makeText(this@MainActivity, "Permission Granted", Toast.LENGTH_SHORT)
            initViewPager()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initViewPager()
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            }
        }
    }

    private fun initViewPager() {
        viewPagerAdapter =
            ViewPagerAdapter(this)
        viewPagerAdapter.addFragments(topSongsFragment, "Top Hits")
        viewPagerAdapter.addFragments(searchFragment, "Search Song")
        viewPagerAdapter.addFragments(favoriteSongFragment, "Favorite Song")
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewPagerAdapter.titles[position]
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ofline -> {
                startActivity(Intent(this, OfflineActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

}