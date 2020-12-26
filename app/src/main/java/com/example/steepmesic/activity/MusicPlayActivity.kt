package com.example.steepmesic.activity

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.steepmesic.R
import com.example.steepmesic.view.RecordView
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_music_play.*
import kotlin.concurrent.thread

class MusicPlayActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_play)

//        setSupportActionBar(music_play_toolbar)
//        music_play_toolbar.setTitle("音乐")
//        supportActionBar?.apply {
//            setTitle("音乐")
//            setHomeAsUpIndicator(R.drawable.ic_down)
//            setDisplayHomeAsUpEnabled(true)
//        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
        supportActionBar?.hide()

        btn_play_pause.setOnClickListener {
//            btn_play_pause.text = "暂停"
//            recordView.isPlaying = !recordView.isPlaying
//            thread {
//                while (recordView.isPlaying || recordView.needleRadiusCounter > RecordView.PAUSE_DEGREE) {
//                    recordView.invalidate()
//                }
//            }
            Glide.with(this).load("https://p1.music.126.net/Nnzk2I8Jl42yCDL3n9s3FA==/109951165563984995.jpg")
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 30)))
                .into(back_img)
        }

    }

}