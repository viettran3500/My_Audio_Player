package com.viet.myaudioplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var actionName: String? = intent?.action
        var serviceIntent: Intent = Intent(context, MusicService::class.java)
        if(actionName != null){
            when(actionName){
                ApplicationClass.ACTION_PLAY->{
                    serviceIntent.putExtra("ActionName", "playPause")
                    context!!.startService(serviceIntent)
                }
                ApplicationClass.ACTION_NEXT->{
                    serviceIntent.putExtra("ActionName", "next")
                    context!!.startService(serviceIntent)
                }
                ApplicationClass.ACTION_PREVIOUS->{
                    serviceIntent.putExtra("ActionName", "previous")
                    context!!.startService(serviceIntent)
                }
                ApplicationClass.ACTION_CLOSE->{
                    serviceIntent.putExtra("ActionName", "close")
                    context!!.startService(serviceIntent)
                }
            }
        }
    }
}