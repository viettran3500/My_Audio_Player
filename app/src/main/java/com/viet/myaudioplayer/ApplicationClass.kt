package com.viet.myaudioplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class ApplicationClass: Application() {

    companion object{
        val ACTION_PREVIOUS = "actionprevious"
        val ACTION_NEXT = "actionnext"
        val ACTION_PLAY = "actionplay"
        val ACTION_CLOSE = "actionclose"
        val CHANNEL_ID_1 = "channel1"
        val CHANNEL_ID_2 = "channel2"
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }



    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel1: NotificationChannel = NotificationChannel(CHANNEL_ID_1, "Channel(1)", NotificationManager.IMPORTANCE_HIGH)
            channel1.description = "Channel 1 Desc..."

            val channel2: NotificationChannel = NotificationChannel(CHANNEL_ID_2, "Channel(2)", NotificationManager.IMPORTANCE_HIGH)
            channel2.description = "Channel 2 Desc..."

            channel2.setSound(null, null)

            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)
        }

    }
}