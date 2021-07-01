package com.viet.myaudioplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.viet.myaudioplayer.activity.PlayerActivity
import com.viet.myaudioplayer.model.MusicFiles
import com.viet.myaudioplayer.model.SongInfo
import java.lang.Exception

class MusicService : Service(), MediaPlayer.OnCompletionListener {

    companion object {
        var shuffleBoolean = false
        var repeatBoolean = 0
        var isOnline = false
        var checkPlayOnline = true
    }

    var mBinder: IBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    var musicFiles: MutableList<MusicFiles> = mutableListOf()
    var musicOnlineFiles: MutableList<SongInfo> = mutableListOf()
    var uri: Uri? = null
    var position: Int = -1
    var actionPlaying: ActionPlaying? = null
    lateinit var mediaSessionCompat: MediaSessionCompat
    var checkLoad = false


    override fun onCreate() {
        mediaSessionCompat = MediaSessionCompat(baseContext, "My Audio")
        super.onCreate()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.e("aaa", "aaa")
        return mBinder
    }

    inner class MyBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val myPosition = intent!!.getIntExtra("servicePosition", -1)
        val actionName: String? = intent.getStringExtra("ActionName")
        if (myPosition != -1) {
            playMedia(myPosition)
        }
        if (actionName != null) {
            when (actionName) {
                "playPause" -> {
                    actionPlaying?.playPauseBtnClick()
                }
                "next" -> {
                    actionPlaying?.nextBtnClick()
                }
                "previous" -> {
                    actionPlaying?.prevBtnClick()
                }
                "close" -> {
                    actionPlaying?.closeBtnClick()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun playMedia(startPosition: Int) {
        if (isOnline) {

            musicOnlineFiles = PlayerActivity.listSongsOnline
            position = startPosition

            if (uri != Uri.parse(musicOnlineFiles[position].source)) {
                if (mediaPlayer != null) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.release()
                    if (musicOnlineFiles.size != 0) {
                        createMediaPlayer(position)
                        mediaPlayer!!.start()
                    }
                } else {
                    createMediaPlayer(position)
                    mediaPlayer!!.start()
                }
            }
        } else {
            musicFiles = PlayerActivity.listSongs
            position = startPosition

            if (uri != Uri.parse(musicFiles[position].path)) {
                if (mediaPlayer != null) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.release()
                    if (musicFiles.size != 0) {
                        createMediaPlayer(position)
                        mediaPlayer!!.start()
                    }
                } else {
                    createMediaPlayer(position)
                    mediaPlayer!!.start()
                }
            }
        }
    }

    fun createMediaPlayer(positionIn: Int) {
        if (isOnline) {
            checkLoad = false
            position = positionIn
            uri = Uri.parse(musicOnlineFiles[position].source)
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            );
            mediaPlayer!!.setDataSource(musicOnlineFiles[position].source)
            mediaPlayer!!.prepareAsync()
            mediaPlayer!!.setOnPreparedListener {
                it.start()
                checkLoad = true
                if (!checkPlayOnline) {
                    it.pause()
                }
            }

        } else {
            position = positionIn
            uri = Uri.parse(musicFiles[position].path)
            Log.d("aaa", "$position")
            mediaPlayer = MediaPlayer.create(baseContext, uri)
        }
    }

    fun start() {
        checkPlayOnline = true
        mediaPlayer!!.start()
    }

    fun pause() {
        checkPlayOnline = false
        mediaPlayer!!.pause()
    }

    fun isPlaying() = mediaPlayer!!.isPlaying

    fun stop() {
        mediaPlayer!!.stop()
        checkPlayOnline = false
    }

    fun release() {
        mediaPlayer!!.release()
    }

    fun getDuration(): Int {
        return if (isOnline) {
            musicOnlineFiles[position].duration * 1000
        } else {
            mediaPlayer!!.duration
        }

    }

    fun seekTo(position: Int) {
        mediaPlayer!!.seekTo(position)
    }

    fun getCurrentPosition() = mediaPlayer!!.currentPosition

    fun onCompleted() {
        mediaPlayer!!.setOnCompletionListener(this)
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (isOnline) {
            if (checkLoad) {
                if (actionPlaying != null) {
                    actionPlaying!!.nextBtnClick()
                    if (mediaPlayer != null) {

                        showNotification(R.drawable.ic_pause)

                        if (position == 0 && repeatBoolean == 0) {
                            actionPlaying!!.playPauseBtnClick()
                        }
                    }
                }
            }
        } else {
            if (actionPlaying != null) {
                actionPlaying!!.nextBtnClick()
                if (mediaPlayer != null) {

                    showNotification(R.drawable.ic_pause)

                    if (position == 0 && repeatBoolean == 0) {
                        actionPlaying!!.playPauseBtnClick()
                    }
                }
            }
        }

    }

    fun setCallBack(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }

    fun showNotification(playPauseBtn: Int) {

        val prevIntent: Intent = Intent(this, NotificationReceiver::class.java).setAction(
            ApplicationClass.ACTION_PREVIOUS
        )
        val prevPending: PendingIntent =
            PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent: Intent = Intent(this, NotificationReceiver::class.java).setAction(
            ApplicationClass.ACTION_PLAY
        )
        val pausePending: PendingIntent =
            PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent: Intent = Intent(this, NotificationReceiver::class.java).setAction(
            ApplicationClass.ACTION_NEXT
        )
        val nextPending: PendingIntent =
            PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val closeIntent: Intent = Intent(this, NotificationReceiver::class.java).setAction(
            ApplicationClass.ACTION_CLOSE
        )
        val closePending: PendingIntent =
            PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var notification: Notification? = null

        if (isOnline) {
//            var thumb: Bitmap? = getAlbumArt(musicFiles[position].path)
//            if (thumb == null) {
//                thumb = BitmapFactory.decodeResource(resources, R.drawable.music)
//            }
            var thumb: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.music)
            notification =
                NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_2)
                    .setSmallIcon(playPauseBtn).setLargeIcon(thumb).setContentTitle(
                        musicOnlineFiles[position].title
                    ).setContentText(musicOnlineFiles[position].artistsNames)
                    .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                    .addAction(playPauseBtn, "Pause", pausePending)
                    .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                    .addAction(R.drawable.ic_close, "Close", closePending)
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSessionCompat.sessionToken)
                            .setShowActionsInCompactView(3)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(null)
                    .setOnlyAlertOnce(true)
                    .build()
        } else {

            var thumb: Bitmap? = getAlbumArt(musicFiles[position].path)
            if (thumb == null) {
                thumb = BitmapFactory.decodeResource(resources, R.drawable.music)
            }

            notification =
                NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID_2)
                    .setSmallIcon(playPauseBtn).setLargeIcon(thumb).setContentTitle(
                        musicFiles[position].title
                    ).setContentText(musicFiles[position].artist)
                    .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                    .addAction(playPauseBtn, "Pause", pausePending)
                    .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                    .addAction(R.drawable.ic_close, "Close", closePending)
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSessionCompat.sessionToken)
                            .setShowActionsInCompactView(3)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(null)
                    .setOnlyAlertOnce(true)
                    .build()

        }
        startForeground(1, notification)
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

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }
}