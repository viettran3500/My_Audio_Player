package com.viet.myaudioplayer.activity

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.adapter.MusicAdapter
import com.viet.myaudioplayer.databinding.ActivityOfflineBinding
import com.viet.myaudioplayer.viewmodel.SongViewModel

class OfflineActivity : AppCompatActivity() {

    private val songViewModel: SongViewModel by lazy {
        ViewModelProvider(
            this,
            SongViewModel.SongViewModelFactory(application)
        )[SongViewModel::class.java]
    }

    lateinit var musicAdapter: MusicAdapter

    private lateinit var binding: ActivityOfflineBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllAudio(this)

        binding.recyclerView.setHasFixedSize(true)
        musicAdapter = MusicAdapter(
            this
        )
        binding.recyclerView.adapter = musicAdapter
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getAllAudio(context: Context) {
        songViewModel.getSongOffObserver().observe(this, Observer {
            MainActivity.musicFiles = it
            musicAdapter.setList(it)
        })

        songViewModel.loadSongOff(context)
    }
}