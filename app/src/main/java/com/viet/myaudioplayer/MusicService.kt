package com.viet.myaudioplayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat

class MusicService(): Service(),MediaPlayer.OnCompletionListener {

    var mBinder: IBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    var musicFiles: MutableList<MusicFiles> = mutableListOf()
    lateinit var uri: Uri
    var position: Int = -1
    var actionPlaying: ActionPlaying? = null
    lateinit var mediaSessionCompat: MediaSessionCompat

    override fun onCreate() {
        mediaSessionCompat = MediaSessionCompat(baseContext,"My Audio")
        super.onCreate()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    inner class MyBinder: Binder() {
        fun getService(): MusicService{
            return this@MusicService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var myPosition = intent!!.getIntExtra("servicePosition", -1)
        val actionName: String? = intent.getStringExtra("ActionName")
        if(myPosition!=-1){
            playMedia(myPosition)
        }
        if(actionName!= null){
            when(actionName){
                "playPause"->{
                    actionPlaying?.playPauseBtnClick()
                }
                "next"->{
                    actionPlaying?.nextBtnClick()
                }
                "previous"->{
                    actionPlaying?.prevBtnClick()
                }
            }
        }
        return START_STICKY
    }

    private fun playMedia(startPosition: Int) {
        musicFiles = PlayerActivity.listSongs
        position = startPosition
        if(mediaPlayer!= null){
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            if(musicFiles.size!=0){
                createMediaPlayer(position)
                mediaPlayer!!.start()
            }
        }else{
            createMediaPlayer(position)
            mediaPlayer!!.start()
        }
    }

    fun start(){
        mediaPlayer!!.start()
    }

    fun pause(){
        mediaPlayer!!.pause()
    }

    fun isPlaying() = mediaPlayer!!.isPlaying

    fun stop(){
        mediaPlayer!!.stop()
    }

    fun release(){
        mediaPlayer!!.release()
    }

    fun getDuration() = mediaPlayer!!.duration

    fun seekTo(position:Int){
        mediaPlayer!!.seekTo(position)
    }

    fun getCurrentPosition() = mediaPlayer!!.currentPosition

    fun createMediaPlayer(positionIn: Int){
        position = positionIn
        uri = Uri.parse(musicFiles[position].path)
        mediaPlayer = MediaPlayer.create(baseContext, uri)
    }

    fun onCompleted(){
        mediaPlayer!!.setOnCompletionListener(this)
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if(actionPlaying != null){
            actionPlaying!!.nextBtnClick()
            if(mediaPlayer!=null){
                createMediaPlayer(position)
                mediaPlayer!!.start()
                onCompleted()
            }
        }
    }

    fun setCallBack(actionPlaying: ActionPlaying){
        this.actionPlaying = actionPlaying
    }

    fun showNotification(playPauseBtn: Int) {
        var intent: Intent = Intent(this, PlayerActivity::class.java)
        var contentIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        var prevIntent: Intent = Intent(this, NotificationReceiver::class.java).setAction(
            ApplicationClass.ACTION_PREVIOUS
        )
        var prevPending: PendingIntent =
            PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var pauseIntent: Intent = Intent(this, NotificationReceiver::class.java).setAction(
            ApplicationClass.ACTION_PLAY
        )
        var pausePending: PendingIntent =
            PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var nextIntent: Intent = Intent(this, NotificationReceiver::class.java).setAction(
            ApplicationClass.ACTION_NEXT
        )
        var nextPending: PendingIntent =
            PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        var picture: ByteArray? = null
        picture = getAlbumArt(musicFiles[position].path)
        var thumb: Bitmap? = null
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.size)
        } else {
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
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.sessionToken))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build()

//        var notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(0, notification)
        startForeground(1, notification)
    }

    fun getAlbumArt(uri: String): ByteArray? {
        var retriever: MediaMetadataRetriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        var art: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return art
    }
}