package com.viet.myaudioplayer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.lang.Exception

class MusicService : Service(), MediaPlayer.OnCompletionListener {

    companion object {
        var shuffleBoolean = false
        var repeatBoolean = 0
    }

    var mBinder: IBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    var musicFiles: MutableList<MusicFiles> = mutableListOf()
    var uri: Uri? = null
    var position: Int = -1
    var actionPlaying: ActionPlaying? = null
    lateinit var mediaSessionCompat: MediaSessionCompat


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

    fun start() {
        mediaPlayer!!.start()
    }

    fun pause() {
        mediaPlayer!!.pause()
    }

    fun isPlaying() = mediaPlayer!!.isPlaying

    fun stop() {
        mediaPlayer!!.stop()
    }

    fun release() {
        mediaPlayer!!.release()
    }

    fun getDuration() = mediaPlayer!!.duration

    fun seekTo(position: Int) {
        mediaPlayer!!.seekTo(position)
    }

    fun getCurrentPosition() = mediaPlayer!!.currentPosition

    fun createMediaPlayer(positionIn: Int) {
        position = positionIn
        uri = Uri.parse(musicFiles[position].path)
        Log.d("aaa", "$position")
        mediaPlayer = MediaPlayer.create(baseContext, uri)
    }

    fun onCompleted() {
        mediaPlayer!!.setOnCompletionListener(this)
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (actionPlaying != null) {
            actionPlaying!!.nextBtnClick()
            if (mediaPlayer != null) {

                createMediaPlayer(position)
                mediaPlayer!!.start()
                showNotification(R.drawable.ic_pause)

                if (position == 0 && repeatBoolean == 0) {
                    actionPlaying!!.playPauseBtnClick()
                }
                onCompleted()
            }
        }
    }

    fun setCallBack(actionPlaying: ActionPlaying) {
        this.actionPlaying = actionPlaying
    }

    fun showNotification(playPauseBtn: Int) {
        //val intent = Intent(this, PlayerActivity::class.java)
        //var contentIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

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

        var thumb: Bitmap? = getAlbumArt(musicFiles[position].albumID)
        if (thumb == null) {
            thumb = BitmapFactory.decodeResource(resources, R.drawable.music)
        }
        val notification: Notification =
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
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(null)
                .setOnlyAlertOnce(true)
                .build()

//        var notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(0, notification)
        startForeground(1, notification)
    }

    private fun getAlbumArt(uri: String): Bitmap? {
        return try {
            val pfd: ParcelFileDescriptor? =
                this.contentResolver.openFileDescriptor(Uri.parse(uri), "r")
            val fileDescriptor = pfd!!.fileDescriptor
            BitmapFactory.decodeFileDescriptor(fileDescriptor)
        } catch (e: Exception) {
            null
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("aaa", "de")
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("aaa", "un")
        return super.onUnbind(intent)
    }
}