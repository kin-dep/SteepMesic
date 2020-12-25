package com.example.steepmesic.activity

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import com.example.steepmesic.R
import kotlinx.android.synthetic.main.activity_music_play.*

class MusicPlayActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_play)

        setSupportActionBar(musicTitle)
        supportActionBar?.apply {
            setTitle("River flows in you")
            setHomeAsUpIndicator(R.drawable.ic_down)
            setDisplayHomeAsUpEnabled(true)
        }

    }

}