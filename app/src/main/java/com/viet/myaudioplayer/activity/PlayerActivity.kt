package com.viet.myaudioplayer.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.ActionPlaying
import com.viet.myaudioplayer.MusicService
import com.viet.myaudioplayer.OnItemClickListener
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.adapter.ListSongRelatedAdapter
import com.viet.myaudioplayer.databinding.ActivityPlayerBinding
import com.viet.myaudioplayer.model.MusicFiles
import com.viet.myaudioplayer.model.SongInfo
import com.viet.myaudioplayer.model.top.ItemSong
import com.viet.myaudioplayer.viewmodel.SongOnlineViewModel
import kotlinx.android.synthetic.main.activity_player.*
import java.util.*

class PlayerActivity : AppCompatActivity(), ActionPlaying, ServiceConnection, OnItemClickListener {

    companion object {
        var listSongs: MutableList<MusicFiles> = mutableListOf()
        var listSongsOnline: MutableList<SongInfo> = mutableListOf()
    }

    var list: MutableList<ItemSong> = mutableListOf()

    private var position = -1

    private var thread: HandlerThread = HandlerThread("Thread")
    private lateinit var playThread: Thread
    private lateinit var prevThread: Thread
    private lateinit var nextThread: Thread
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var runnablePlus: Runnable
    private lateinit var runnableMinus: Runnable

    var musicService: MusicService? = null

    private lateinit var listSongRelatedAdapter: ListSongRelatedAdapter

    private val songOnlineViewModel: SongOnlineViewModel by lazy {
        ViewModelProvider(
            this,
            SongOnlineViewModel.SongViewModelFactory(this.application)
        )[SongOnlineViewModel::class.java]
    }

    private lateinit var binding: ActivityPlayerBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listSongRelatedAdapter = ListSongRelatedAdapter(this, this)
        binding.recyclerViewRelated.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewRelated.adapter = listSongRelatedAdapter

        thread.start()
        handler = Handler(thread.looper)

        getIntentMethod()

        runnable = Runnable {
            if (musicService != null) {
                val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                binding.seekBar.progress = mCurrentPosition
                this.runOnUiThread {
                    binding.tvDurationPlayer.text = formattedtime(mCurrentPosition)
                }
            }
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)

        runnablePlus = Runnable {
            binding.seekBar.progress = binding.seekBar.progress + 1
            musicService!!.seekTo(binding.seekBar.progress * 1000)
            this.runOnUiThread {
                binding.tvDurationPlayer.text = formattedtime(binding.seekBar.progress)
            }
            handler.postDelayed(runnablePlus, 100)
        }
        binding.btnFastForward.setOnTouchListener { p0, motionEvent ->
            if (motionEvent != null) {
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    handler.postDelayed(runnablePlus, 1000)
                }
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(runnablePlus)
                }
            }
            true
        }

        runnableMinus = Runnable {
            binding.seekBar.progress = binding.seekBar.progress - 1
            musicService!!.seekTo(binding.seekBar.progress * 1000)
            this.runOnUiThread {
                binding.tvDurationPlayer.text = formattedtime(binding.seekBar.progress)
            }
            handler.postDelayed(runnableMinus, 100)
        }

        binding.btnFastRewind.setOnTouchListener { p0, motionEvent ->
            if (motionEvent != null) {
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    handler.postDelayed(runnableMinus, 1000)
                }
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(runnableMinus)
                }
            }
            true
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (musicService != null && fromUser) {
                    musicService!!.seekTo(progress * 1000)
                    binding.tvDurationPlayer.text = formattedtime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        checkShuffleRepeat()

        binding.btnShuffle.setOnClickListener {
            if (MusicService.shuffleBoolean) {
                MusicService.shuffleBoolean = false
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
            } else {
                MusicService.shuffleBoolean = true
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_on)
            }
        }

        binding.btnRepeat.setOnClickListener {
            when (MusicService.repeatBoolean) {
                2 -> {
                    MusicService.repeatBoolean = 0
                    binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
                }
                1 -> {
                    MusicService.repeatBoolean = 2
                    binding.btnRepeat.setImageResource(R.drawable.ic_repeat_on)
                }
                0 -> {
                    MusicService.repeatBoolean = 1
                    binding.btnRepeat.setImageResource(R.drawable.ic_repeat_1)
                }
            }
        }
    }

    private fun checkShuffleRepeat() {
        if (MusicService.shuffleBoolean)
            binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_on)
        else
            binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)

        when (MusicService.repeatBoolean) {
            2 -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_on)
            }
            1 -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_1)
            }
            0 -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
            }
        }

        if (MusicService.checkPlayOnline) {
            vumeter.resume(true)
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_pause)

        } else {
            vumeter.pause()
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
    }

    override fun onResume() {
        handler.post(runnable)
        checkShuffleRepeat()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
        playThreadBtn()
        prevThreadBtn()
        nextThreadBtn()
        super.onResume()
    }

    override fun onPause() {
        handler.removeCallbacks(runnablePlus)
        handler.removeCallbacks(runnableMinus)
        handler.removeCallbacks(runnable)
        unbindService(this)
        super.onPause()
    }

    private fun prevThreadBtn() {
        prevThread = Thread(object : Thread() {
            override fun run() {
                super.run()
                binding.btnPerv.setOnClickListener {
                    prevBtnClick()
                }
            }
        })
        prevThread.start()
    }

    override fun prevBtnClick() {
        handler.removeCallbacks(runnablePlus)
        handler.removeCallbacks(runnableMinus)
        handler.removeCallbacks(runnable)
        if (MusicService.checkPlayOnline) {
            musicService!!.stop()
            musicService!!.release()
            if (MusicService.isOnline) {
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongsOnline.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = if ((position - 1) < 0) listSongsOnline.size - 1 else position - 1
                }
                metaData()
                binding.tvSongName.text = listSongsOnline[position].title
                binding.tvSongArtist.text = listSongsOnline[position].artistsNames
            } else {
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongs.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = if ((position - 1) < 0) listSongs.size - 1 else position - 1
                }
                metaData()
                binding.tvSongName.text = listSongs[position].title
                binding.tvSongArtist.text = listSongs[position].artist
            }

            musicService!!.createMediaPlayer(position)
            binding.seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            vumeter.resume(true)
            musicService!!.showNotification(R.drawable.ic_pause)
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()
            if (MusicService.isOnline) {
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongsOnline.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = if ((position - 1) < 0) listSongsOnline.size - 1 else position - 1
                }
                metaData()
                binding.tvSongName.text = listSongsOnline[position].title
                binding.tvSongArtist.text = listSongsOnline[position].artistsNames
            } else {
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongs.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = if ((position - 1) < 0) listSongs.size - 1 else position - 1
                }
                metaData()
                binding.tvSongName.text = listSongs[position].title
                binding.tvSongArtist.text = listSongs[position].artist
            }


            musicService!!.createMediaPlayer(position)
            binding.seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            vumeter.pause()
            musicService!!.showNotification(R.drawable.ic_play)
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
    }

    private fun nextThreadBtn() {
        nextThread = Thread(object : Thread() {
            override fun run() {
                super.run()
                binding.btnNext.setOnClickListener {
                    nextBtnClick()
                }
            }
        })
        nextThread.start()
    }

    override fun nextBtnClick() {
        handler.removeCallbacks(runnablePlus)
        handler.removeCallbacks(runnableMinus)
        handler.removeCallbacks(runnable)
        if (/*musicService!!.isPlaying()*/MusicService.checkPlayOnline) {
            musicService!!.stop()
            musicService!!.release()
            if (MusicService.isOnline) {
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongsOnline.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = (position + 1) % listSongsOnline.size
                }
                metaData()
                binding.tvSongName.text = listSongsOnline[position].title
                binding.tvSongArtist.text = listSongsOnline[position].artistsNames
            } else {
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongs.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = (position + 1) % listSongs.size
                }

                metaData()
                binding.tvSongName.text = listSongs[position].title
                binding.tvSongArtist.text = listSongs[position].artist
            }

            binding.seekBar.progress = 0
            musicService!!.createMediaPlayer(position)
            binding.seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            vumeter.resume(true)
            musicService!!.showNotification(R.drawable.ic_pause)
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()
            if (MusicService.isOnline) {
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongsOnline.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = (position + 1) % listSongsOnline.size
                }
                metaData()
                binding.tvSongName.text = listSongsOnline[position].title
                binding.tvSongArtist.text = listSongsOnline[position].artistsNames
            } else {
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongs.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = (position + 1) % listSongs.size
                }
                metaData()
                binding.tvSongName.text = listSongs[position].title
                binding.tvSongArtist.text = listSongs[position].artist
            }


            musicService!!.createMediaPlayer(position)
            binding.seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            vumeter.pause()
            musicService!!.showNotification(R.drawable.ic_play)
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
    }

    override fun closeBtnClick() {
        handler.removeCallbacks(runnablePlus)
        handler.removeCallbacks(runnableMinus)
        handler.removeCallbacks(runnable)
        musicService!!.stopSelf()
        finish()
    }

    override fun changeBtnClick() {
        binding.btnPlayPause.setBackgroundResource(R.drawable.ic_play)
    }

    private fun getRandom(i: Int): Int {
        val random = Random()
        var posi = random.nextInt(i + 1)
        while (posi == position) {
            posi = random.nextInt(i + 1)
        }
        return posi
    }

    private fun playThreadBtn() {
        playThread = Thread(object : Thread() {
            override fun run() {
                super.run()
                binding.btnPlayPause.setOnClickListener {
                    playPauseBtnClick()
                }
            }
        })
        playThread.start()
    }

    override fun playPauseBtnClick() {
        if (MusicService.checkPlayOnline) {
            MusicService.checkPlayOnline = false
            vumeter.pause()
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            musicService!!.showNotification(R.drawable.ic_play)
            musicService!!.pause()
            binding.seekBar.max = musicService!!.getDuration() / 1000
            handler.removeCallbacks(runnable)
        } else {
            MusicService.checkPlayOnline = true
            vumeter.resume(true)
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause)
            musicService!!.start()
            binding.seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
        }
    }

    private fun formattedtime(mCurrentPosition: Int): String {
        val totalOut: String
        val totalNew: String
        val seconds: String = (mCurrentPosition % 60).toString()
        val minutes: String = (mCurrentPosition / 60).toString()
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"
        return if (seconds.length == 1) {
            totalNew
        } else {
            totalOut
        }
    }

    private fun getIntentMethod() {
        position = intent.getIntExtra("position", -1)

        when (intent.getStringExtra("sender")) {
            "favoriteSong" -> {
                MusicService.isOnline = true
                listSongsOnline = MainActivity.listMusicFavorite
            }
            "searchSong" -> {
                MusicService.isOnline = true
                listSongsOnline = MainActivity.listMusicSearch
            }
            "topSong" -> {
                MusicService.isOnline = true
                listSongsOnline = MainActivity.listMusicTop
            }
            null -> {
                listSongs = MainActivity.musicFiles
                MusicService.isOnline = false
            }
        }

        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("servicePosition", position)
        startService(intent)
    }

    private fun metaData() {
        val durationTotal: Int
        if (MusicService.isOnline) {
            durationTotal = listSongsOnline[position].duration
            loadRelatedSong()
            loadInfo()
        } else {
            durationTotal = listSongs[position].duration.toInt() / 1000
        }
        binding.tvDurationTotal.text = formattedtime(durationTotal)
    }

    private fun loadInfo() {
        songOnlineViewModel.getInfoObserver().observe(this, Observer {
            var genre = ""
            for (i in 0 until it.genres.size) {
                genre += if (i != it.genres.size - 1)
                    "${it.genres[i].name}, "
                else
                    it.genres[i].name
            }
            binding.tvGenre.text = genre
            Log.e("aaa", genre)
        })
        songOnlineViewModel.loadInfo(listSongsOnline[position].id)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder: MusicService.MyBinder = service as MusicService.MyBinder
        musicService = myBinder.getService()

        musicService!!.setCallBack(this)

        binding.seekBar.max = musicService!!.getDuration() / 1000
        metaData()

        if (MusicService.isOnline) {
            binding.tvSongName.text = listSongsOnline[position].title
            binding.tvSongArtist.text = listSongsOnline[position].artistsNames
        } else {
            binding.tvSongName.text = listSongs[position].title
            binding.tvSongArtist.text = listSongs[position].artist
        }

        musicService!!.onCompleted()
        if (MusicService.checkPlayOnline) {
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause)
        } else {
            musicService!!.showNotification(R.drawable.ic_play)
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }

    }

    private fun loadRelatedSong() {
        binding.progressBarLoadingRecommend.visibility = View.VISIBLE
        songOnlineViewModel.getListSongRelatedObserver().observe(this, Observer {
            binding.progressBarLoadingRecommend.visibility = View.GONE
            listSongRelatedAdapter.setListSong(it)
        })
        songOnlineViewModel.loadRelatedSong(listSongsOnline[position].id)
    }

    override fun onItemClick(pos: Int) {
        MusicService.isOnline = true
        listSongsOnline = MainActivity.listSongRelated
        handler.removeCallbacks(runnablePlus)
        handler.removeCallbacks(runnableMinus)
        handler.removeCallbacks(runnable)

        if (/*musicService!!.isPlaying()*/MusicService.checkPlayOnline) {
            musicService!!.stop()
            musicService!!.release()

            position = pos
            metaData()
            binding.tvSongName.text = listSongsOnline[position].title
            binding.tvSongArtist.text = listSongsOnline[position].artistsNames

            musicService!!.createMediaPlayer(position)
            binding.seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            vumeter.resume(true)
            musicService!!.showNotification(R.drawable.ic_pause)
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()

            position = pos
            metaData()
            binding.tvSongName.text = listSongsOnline[position].title
            binding.tvSongArtist.text = listSongsOnline[position].artistsNames

            musicService!!.createMediaPlayer(position)
            binding.seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            vumeter.pause()
            musicService!!.showNotification(R.drawable.ic_play)
            binding.btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
    }
}