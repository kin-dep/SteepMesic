package com.example.steepmesic.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.steepmesic.activity.MusicPlayActivity
import com.example.steepmesic.service.MusicService

class MusicReceiver : BroadcastReceiver() {

    companion object {
        const val UPDATE_UI_BROADCAST = "com.example.steepmesic.receiver.updateui"
        const val UPDATE_MUSIC_SOURCE = "com.example.steepmesic.receiver.updatesource"
        const val UPDATE_MUSIC_IMG = "com.example.steepmesic.receiver.updatimg"
        const val UPDATE_MUSIC_TEXT = "com.example.steepmesic.receiver.updatetext"
        const val UPDATE_PLAY_PAUSE_STATE = "com.example.steepmesic.receiver.updatestate"
        const val MUSIC_NOTIFIED_PREV = "prev"
        const val MUSIC_NOTIFIED_NEXT = "next"
        const val MUSIC_NOTIFIED_PLAY = "play"
        const val MUSIC_NOTIFIED_STOP = "pause"
        const val TARGET_FLAG = "flag"
    }

    //切换音乐时更新界面
    override fun onReceive(context: Context, intent: Intent) {
        if (context is MusicService) {
            context as MusicService
            when (intent.getStringExtra(TARGET_FLAG)) {
                MUSIC_NOTIFIED_PLAY -> context.play()
                MUSIC_NOTIFIED_STOP -> context.pause()
                MUSIC_NOTIFIED_NEXT -> {
                    Log.d("MUSIC_NOTIFIED_NEXT", "enter")
                    context.next()
                }
                MUSIC_NOTIFIED_PREV -> context.prev()
            }
        } else if (context is MusicPlayActivity) {
            context as MusicPlayActivity
            when (intent.action) {
                UPDATE_MUSIC_TEXT -> context.showMusicTextInfo()
                UPDATE_MUSIC_IMG -> context.showMusicImgInfo()
                UPDATE_MUSIC_SOURCE -> context.showMusicSourceInfo()
                UPDATE_PLAY_PAUSE_STATE -> {
                    val resId = intent.getIntExtra("destResId", -1)
                    if (resId != -1) context.changePlayPause(resId)
                }
            }
        }
    }

}