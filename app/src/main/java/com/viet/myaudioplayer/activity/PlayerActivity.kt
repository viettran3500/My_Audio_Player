package com.viet.myaudioplayer.activity

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.ActionPlaying
import com.viet.myaudioplayer.MusicService
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.adapter.ListSongRelatedAdapter
import com.viet.myaudioplayer.api.ApiService
import com.viet.myaudioplayer.model.MusicFiles
import com.viet.myaudioplayer.model.SongInfo
import com.viet.myaudioplayer.model.infomusic.DataInfo
import com.viet.myaudioplayer.model.recommend.ItemRecommend
import com.viet.myaudioplayer.model.top.ItemSong
import kotlinx.android.synthetic.main.activity_player.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class PlayerActivity : AppCompatActivity(), ActionPlaying, ServiceConnection {

    companion object {
        var listSongs: MutableList<MusicFiles> = mutableListOf()
        var listSongsOnline: MutableList<SongInfo> = mutableListOf()
        var listSongRelated: MutableList<SongInfo> = mutableListOf()
    }

    var list: MutableList<ItemSong> = mutableListOf()

    private var position = -1
    //private lateinit var uri: Uri

    private var thread: HandlerThread = HandlerThread("Thread")
    private lateinit var playThread: Thread
    private lateinit var prevThread: Thread
    private lateinit var nextThread: Thread
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var runnablePlus: Runnable
    private lateinit var runnableMinus: Runnable

    var musicService: MusicService? = null

    lateinit var listSongRelatedAdapter: ListSongRelatedAdapter

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        listSongRelatedAdapter = ListSongRelatedAdapter(this)
        recyclerViewRelated.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerViewRelated.adapter = listSongRelatedAdapter

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

        runnablePlus = Runnable {
            seekBar.progress = seekBar.progress + 1
            musicService!!.seekTo(seekBar.progress * 1000)
            this.runOnUiThread {
                tvDurationPlayer.text = formattedtime(seekBar.progress)
            }
            handler.postDelayed(runnablePlus,100)
        }
        btnFastForward.setOnTouchListener { p0, motionEvent ->
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

        runnableMinus= Runnable {
            seekBar.progress = seekBar.progress - 1
            musicService!!.seekTo(seekBar.progress * 1000)
            this.runOnUiThread {
                tvDurationPlayer.text = formattedtime(seekBar.progress)
            }
            handler.postDelayed(runnableMinus,100)
        }

        btnFastRewind.setOnTouchListener { p0, motionEvent ->
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
            when (MusicService.repeatBoolean) {
                2 -> {
                    MusicService.repeatBoolean = 0
                    btnRepeat.setImageResource(R.drawable.ic_repeat_off)
                }
                1 -> {
                    MusicService.repeatBoolean = 2
                    btnRepeat.setImageResource(R.drawable.ic_repeat_on)
                }
                0 -> {
                    MusicService.repeatBoolean = 1
                    btnRepeat.setImageResource(R.drawable.ic_repeat_1)
                }
            }
        }
    }

    private fun checkShuffleRepeat() {
        if (MusicService.shuffleBoolean)
            btnShuffle.setImageResource(R.drawable.ic_shuffle_on)
        else
            btnShuffle.setImageResource(R.drawable.ic_shuffle_off)

        when (MusicService.repeatBoolean) {
            2 -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat_on)
            }
            1 -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat_1)
            }
            0 -> {
                btnRepeat.setImageResource(R.drawable.ic_repeat_off)
            }
        }

        if(musicService != null) {
            if (musicService!!.isPlaying())
                btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            else
                btnPlayPause.setBackgroundResource(R.drawable.ic_play)
        }
    }

    override fun onResume() {

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
                btnPerv.setOnClickListener {
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
        if (musicService!!.isPlaying()) {
            musicService!!.stop()
            musicService!!.release()
            if(MusicService.isOnline){
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongsOnline.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = if ((position - 1) < 0) listSongsOnline.size - 1 else position - 1
                }
                //uri = Uri.parse(listSongs[position].path)
                metaData()
                tvSongName.text = listSongsOnline[position].title
                tvSongArtist.text = listSongsOnline[position].artistsNames
            }else{
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongs.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = if ((position - 1) < 0) listSongs.size - 1 else position - 1
                }
                //uri = Uri.parse(listSongs[position].path)
                metaData()
                tvSongName.text = listSongs[position].title
                tvSongArtist.text = listSongs[position].artist
            }

            musicService!!.createMediaPlayer(position)
            seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            musicService!!.showNotification(R.drawable.ic_pause)
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()
            if(MusicService.isOnline){
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongsOnline.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = if ((position - 1) < 0) listSongsOnline.size - 1 else position - 1
                }
                //uri = Uri.parse(listSongs[position].path)
                metaData()
                tvSongName.text = listSongsOnline[position].title
                tvSongArtist.text = listSongsOnline[position].artistsNames
            }else{
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongs.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = if ((position - 1) < 0) listSongs.size - 1 else position - 1
                }
                //uri = Uri.parse(listSongs[position].path)
                metaData()
                tvSongName.text = listSongs[position].title
                tvSongArtist.text = listSongs[position].artist
            }


            musicService!!.createMediaPlayer(position)
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
        handler.removeCallbacks(runnablePlus)
        handler.removeCallbacks(runnableMinus)
        handler.removeCallbacks(runnable)
        if (musicService!!.isPlaying()) {
            musicService!!.stop()
            musicService!!.release()
            if(MusicService.isOnline){
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongsOnline.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = (position + 1) % listSongsOnline.size
                }
                //uri = Uri.parse(listSongs[position].path)
                metaData()
                tvSongName.text = listSongsOnline[position].title
                tvSongArtist.text = listSongsOnline[position].artistsNames
            }else{
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongs.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = (position + 1) % listSongs.size
                }
                //uri = Uri.parse(listSongs[position].path)
                metaData()
                tvSongName.text = listSongs[position].title
                tvSongArtist.text = listSongs[position].artist
            }


            musicService!!.createMediaPlayer(position)
            seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            musicService!!.showNotification(R.drawable.ic_pause)
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause)
            musicService!!.start()
        } else {
            musicService!!.stop()
            musicService!!.release()
            if(MusicService.isOnline){
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongsOnline.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = (position + 1) % listSongsOnline.size
                }
                //uri = Uri.parse(listSongs[position].path)
                metaData()
                tvSongName.text = listSongsOnline[position].title
                tvSongArtist.text = listSongsOnline[position].artistsNames
            }else{
                if (MusicService.shuffleBoolean && MusicService.repeatBoolean == 1) {
                    position = getRandom(listSongs.size - 1)
                } else if (!MusicService.shuffleBoolean && (MusicService.repeatBoolean == 2 || MusicService.repeatBoolean == 0)) {
                    position = (position + 1) % listSongs.size
                }
                //uri = Uri.parse(listSongs[position].path)
                metaData()
                tvSongName.text = listSongs[position].title
                tvSongArtist.text = listSongs[position].artist
            }


            musicService!!.createMediaPlayer(position)
            seekBar.max = musicService!!.getDuration() / 1000
            handler.post(runnable)
            musicService!!.onCompleted()
            musicService!!.showNotification(R.drawable.ic_play)
            btnPlayPause.setBackgroundResource(R.drawable.ic_play)
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
        btnPlayPause.setBackgroundResource(R.drawable.ic_play)
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
        val sender: String? = intent.getStringExtra("sender")

        when(sender){
            "favoriteSong"->{
                MusicService.isOnline = true
                listSongsOnline = MainActivity.listMusicFavorite
            }
            "searchSong"->{
                MusicService.isOnline = true
                listSongsOnline = MainActivity.listMusicSearch
            }
            "topSong"->{
                MusicService.isOnline = true
                listSongsOnline = MainActivity.listMusicTop
            }
            null->{
                listSongs = MainActivity.musicFiles
                MusicService.isOnline = false
            }
        }

        btnPlayPause.setImageResource(R.drawable.ic_pause)
        //uri = Uri.parse(listSongs[position].path)
        val intent: Intent = Intent(this, MusicService::class.java)
        intent.putExtra("servicePosition", position)
        startService(intent)
    }

    private fun metaData() {
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(this, uri)
        val durationTotal: Int
        if(MusicService.isOnline){
            durationTotal = listSongsOnline[position].duration
            loadRelatedSong()
        }else{
            durationTotal = listSongs[position].duration.toInt() / 1000
        }
        tvDurationTotal.text = formattedtime(durationTotal)
//        val art: ByteArray? = retriever.embeddedPicture
//        if (art != null) {
//            imgCoverArt.setImageBitmap(BitmapFactory.decodeByteArray(art, 0, art.size))
//        } else {
//            imgCoverArt.setImageResource(R.drawable.music)
//        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val myBinder: MusicService.MyBinder = service as MusicService.MyBinder
        musicService = myBinder.getService()

        musicService!!.setCallBack(this)

        seekBar.max = musicService!!.getDuration() / 1000
        metaData()

        if(MusicService.isOnline){
            tvSongName.text = listSongsOnline[position].title
            tvSongArtist.text = listSongsOnline[position].artistsNames
        }else{
            tvSongName.text = listSongs[position].title
            tvSongArtist.text = listSongs[position].artist
        }

        musicService!!.onCompleted()
        musicService!!.showNotification(R.drawable.ic_pause)
    }

    private fun loadRelatedSong() {

        progressBarLoadingRecommend.visibility = View.VISIBLE

        ApiService.apiService.getSongRelated(listSongsOnline[position].id).enqueue(object : Callback<ItemRecommend> {
            override fun onFailure(call: Call<ItemRecommend>, t: Throwable) {
            }

            override fun onResponse(call: Call<ItemRecommend>, response: Response<ItemRecommend>) {
                var item: ItemRecommend? = response.body()
                if (item != null && item.err == 0) {
                    list = item.data.items as MutableList<ItemSong>
                    loadInfo()
                }
            }
        })
    }

    private fun loadInfo() {
        listSongRelated.clear()
        for (i in 0 until list.size) {
            listSongRelated.add(SongInfo(list[i].id,list[i].title,list[i].artistsNames,list[i].thumbnail,list[i].duration, null, null))
            ApiService.apiService.getInfoMusic(list[i].id).enqueue(object : Callback<DataInfo> {
                override fun onFailure(call: Call<DataInfo>, t: Throwable) {
                }

                override fun onResponse(call: Call<DataInfo>, response: Response<DataInfo>) {
                    val dataInfo: DataInfo? = response.body()
                    if (dataInfo != null && dataInfo.err == 0) {
                        var genre = ""
                        for (element in dataInfo.data.genres){
                            genre += "${element.name} "
                        }
                        listSongRelated[i].genre = genre
                        listSongRelated[i].source = "http://api.mp3.zing.vn/api/streaming/audio/${listSongRelated[i].id}/128"

                        progressBarLoadingRecommend.visibility = View.GONE
                        listSongRelatedAdapter.setListSong(listSongRelated)
                    }
                }

            })
        }
    }
}