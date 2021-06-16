package com.viet.myaudioplayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_player.*
import java.util.*

class PlayerActivity : AppCompatActivity(), ActionPlaying, ServiceConnection {

    companion object {
        var listSongs: MutableList<MusicFiles> = mutableListOf()
    }

    var position = -1
    lateinit var uri: Uri

    var thread: HandlerThread = HandlerThread("Thread")
    lateinit var playThread: Thread
    lateinit var prevThread: Thread
    lateinit var nextThread: Thread
    lateinit var handler: Handler
    lateinit var runnable: Runnable

    var musicService: MusicService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        thread.start()
        handler = Handler(thread.looper)

        getIntentMethod()

        runnable = Runnable {
            if (musicService != null) {
                val mCurrentPosition = musicService!!.getCurrentPosition() / 1000
                seekBar.progress = mCurrentPosition
                this.runOnUiThread {
                    tvDurationPlayer.text = formattedtime(mCurrentPosition)
                }
            }
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (musicService != null && fromUser) {
                    musicService!!.seekTo(progress * 1000)
                    tvDurationPlayer.text = formattedtime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        checkShuffleRepeat()

        btnShuffle.setOnClickListener {
            if (MusicService.shuffleBoolean) {
                MusicService.shuffleBoolean = false
                btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
            } else {
                MusicService.shuffleBoolean = true
                btnShuffle.setImageResource(R.drawable.ic_shuffle_on)
            }
        }

        btnRepeat.setOnClickListener {
            when(MusicService.repeatBoolean){
                2->{
                    MusicService.repeatBoolean = 0
                    btnRepeat.setImageResource(R.drawable.ic_repeat_off)
                }
                1-> {
                    MusicService.repeatBoolean = 2
                    btnRepeat.setImageResource(R.drawable.ic_repeat_on)
                }
                0->{
                    MusicService.repeatBoolean = 1
                    btnRepeat.setImageResource(R.drawable.ic_repeat_1)
                }
            }
        }
    }

    fun checkShuffleRepeat(){
        if(MusicService.shuffleBoolean)
            btnShuffle.setImageResource(R.drawable.ic_shuffle_on)
        else
            btnShuffle.setImageResource(R.drawable.ic_shuffle_off)

        when(MusicService.repeatBoolean){
            2->{
                btnRepeat.setImageResource(R.drawable.ic_repeat_on)
            }
            1-> {
                btnRepeat.setImageResource(R.drawable.ic_repeat_1)
            }
            0->{
                btnRepeat.setImageResource(R.drawable.ic_repeat_off)
            }
        }
    }

    override fun onResume() {

        val intent: Intent = Intent(this, MusicService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
        playThreadBtn()
        prevThreadBtn()
        nextThreadBtn()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
        unbindService(this)
    }

    private fun prevThreadBtn() {
        prevThread = Thread(object : Thread() {
            override fun run() {
                super.run()
                btnPerv.setOnClickListener {
                    prevBtnClick()
                }
            }
        })
        prevThread.start()
    }

    override fun prevBtnClick() {
        handler.removeCallbacks(runnable)
        if (musicService!!.isPlaying()) {
            musicService!!.stop()
            musicService!!.release()
            if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                position = getRandom(listSongs.size - 1)
            } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                position = if ((position - 1) < 0) listSongs.size - 1 else position - 1
            }

            uri = Uri.parse(listSongs[position].path)
            musicService!!.createMediaPlayer(position)
            metaData(uri)
            tvSongName.text = listSongs[position].title
            tvSongArtist.text = listSongs[position].artist
            seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            musicService!!.showNotification(R.drawable.ic_pause)
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()
            if (MusicService.shuffleBoolean && MusicService.repeatBoolean ==1) {
                position = getRandom(listSongs.size - 1)
            } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                position = if ((position - 1) < 0) listSongs.size - 1 else position - 1
            }
            uri = Uri.parse(listSongs[position].path)
            musicService!!.createMediaPlayer(position)
            metaData(uri)
            tvSongName.text = listSongs[position].title
            tvSongArtist.text = listSongs[position].artist
            seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            musicService!!.showNotification(R.drawable.ic_play)
            btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
    }

    private fun nextThreadBtn() {
        nextThread = Thread(object : Thread() {
            override fun run() {
                super.run()
                btnNext.setOnClickListener {
                    nextBtnClick()
                }
            }
        })
        nextThread.start()
    }

    override fun nextBtnClick() {
        handler.removeCallbacks(runnable)
        if (musicService!!.isPlaying()) {
            musicService!!.stop()
            musicService!!.release()
            if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                position = getRandom(listSongs.size - 1)
            } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                position = (position + 1) % listSongs.size
            }
            uri = Uri.parse(listSongs[position].path)
            musicService!!.createMediaPlayer(position)
            metaData(uri)
            tvSongName.text = listSongs[position].title
            tvSongArtist.text = listSongs[position].artist
            seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            musicService!!.showNotification(R.drawable.ic_pause)
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()
            if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                position = getRandom(listSongs.size - 1)
            } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                position = (position + 1) % listSongs.size
            }
            uri = Uri.parse(listSongs[position].path)
            musicService!!.createMediaPlayer(position)
            metaData(uri)
            tvSongName.text = listSongs[position].title
            tvSongArtist.text = listSongs[position].artist
            seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            musicService!!.showNotification(R.drawable.ic_play)
            btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
    }

    override fun closeBtnClick() {
        musicService!!.stopSelf()
        finish()
    }

    override fun changeBtnClick() {
        btnPlayPause.setBackgroundResource(R.drawable.ic_play)
    }

    private fun getRandom(i: Int): Int {
        val random: Random = Random()
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
                btnPlayPause.setOnClickListener {
                    playPauseBtnClick()
                }
            }
        })
        playThread.start()
    }

    override fun playPauseBtnClick() {
        if (musicService!!.isPlaying()) {
            btnPlayPause.setImageResource(R.drawable.ic_play)
            musicService!!.showNotification(R.drawable.ic_play)
            musicService!!.pause()
            seekBar.max = musicService!!.getDuration() / 1000
            handler.removeCallbacks(runnable)
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause)
            musicService!!.start()
            seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
        }
    }

    private fun formattedtime(mCurrentPosition: Int): String {
        var totalOut = ""
        var totalNew = ""
        val seconds: String = (mCurrentPosition % 60).toString()
        val minutes: String = (mCurrentPosition / 60).toString()
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"
        if (seconds.length == 1) {
            return totalNew
        } else {
            return totalOut
        }
    }

    private fun getIntentMethod() {
        position = intent.getIntExtra("position", -1)
        val sender: String? = intent.getStringExtra("sender")

        listSongs = if (sender != null && sender == "albumDetails") {
            AlbumDetails.albumSongs
        } else {
            MainActivity.musicFiles
        }
        btnPlayPause.setImageResource(R.drawable.ic_pause)
        uri = Uri.parse(listSongs[position].path)
        val intent: Intent = Intent(this, MusicService::class.java)
        intent.putExtra("servicePosition", position)
        startService(intent)
    }

    fun metaData(uri: Uri) {
        var retriever: MediaMetadataRetriever = MediaMetadataRetriever()
        retriever.setDataSource(uri.toString())
        var durationTotal: Int = listSongs[position].duration.toInt() / 1000
        tvDurationTotal.text = formattedtime(durationTotal)
        var art: ByteArray? = retriever.embeddedPicture
        if (art != null) {
            imgCoverArt.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.size))
        } else {
            imgCoverArt.setImageResource(R.drawable.music)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder: MusicService.MyBinder = service as MusicService.MyBinder
        musicService = myBinder.getService()

        musicService!!.setCallBack(this)

        seekBar.max = musicService!!.getDuration() / 1000
        metaData(uri)

        tvSongName.text = listSongs[position].title
        tvSongArtist.text = listSongs[position].artist
        musicService!!.onCompleted()
        musicService!!.showNotification(R.drawable.ic_pause)
    }

}