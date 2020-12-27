package com.example.steepmesic.activity

import android.graphics.*
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.steepmesic.R
import com.example.steepmesic.net.MusicApi
import com.example.steepmesic.pojo.minfo.MusicInfo
import com.example.steepmesic.pojo.murl.MusicUrlData
import com.example.steepmesic.util.NetUtil
import com.example.steepmesic.view.RecordView
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_music_play.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class MusicPlayActivity : BaseActivity() {

    private lateinit var musicApi: MusicApi
    //音乐播放列表
    private lateinit var musicIds: ArrayList<Int>
    //当前播放位置
    private var pos: Int = -1
    //歌曲信息
    private var id = -1 //歌曲id
    private lateinit var musicName: String
    private lateinit var musicArtist: String
    private lateinit var picUrl: String
    private lateinit var picImg: Bitmap
    private lateinit var musicSource: String
//    private var current = 0 //当前播放位置
//    private var total = -1 //歌曲时长
    //播放器
    private lateinit var mp: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_play)

        //初始颜色调暗
        back_img.setBackgroundColor(Color.parseColor("#333333"))

        //初始化
        musicApi = NetUtil.create(MusicApi::class.java)
        mp = MediaPlayer()

        //获取传入的数据
        musicIds = intent.getIntegerArrayListExtra("musicIds")!!
        pos = intent.getIntExtra("pos", -1)!!
        if (pos != -1) {
            id = musicIds[pos]
            loadMusic(id)
        }

        //加载好歌曲后自动开始播放
        mp.setOnPreparedListener {
            mp.start()
            music_progressbar.max = mp.duration
            thread {
                recordView.isPlaying = true
                while (recordView.isPlaying || recordView.needleRadiusCounter > RecordView.PAUSE_DEGREE) {
                    recordView.invalidate()
                }
            }
            //计算时长
            //...
            thread {
                while (true) {
                    Thread.sleep(100)
                    music_progressbar.progress = mp.currentPosition
                    //计算当前位置对应的时间
                    //...
                }
            }
            //进度控制事件监听
            music_progressbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?,
                                               progress: Int,
                                               fromUser: Boolean) {
                    if (fromUser) { mp.seekTo(progress) }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            //控制播放与暂停
            btn_play_pause.setOnClickListener {
                if (mp.isPlaying) {
                    mp.pause()
                    recordView.isPlaying = false
                    thread {
                        while (recordView.isPlaying || recordView.needleRadiusCounter > RecordView.PAUSE_DEGREE) {
                            recordView.invalidate()
                        }
                    }
                } else {
                    mp.start()
                    recordView.isPlaying = true
                    thread {
                        while (recordView.isPlaying || recordView.needleRadiusCounter > RecordView.PAUSE_DEGREE) {
                            recordView.invalidate()
                        }
                    }
                }
            }
        }

        //下一首
        btn_next.setOnClickListener {
            id = musicIds[(++pos) % musicIds.size]
            loadMusic(id)
        }

        //上一首
        btn_prev.setOnClickListener {
            pos --
            if (pos < 0) pos = musicIds.size - 1
            id = musicIds[pos]
            loadMusic(id)
        }

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

        }

    }

    //加载音乐源和图片
    private fun loadMusic(id: Int) {
        //加载图片
        loadImage(id)
        loadData(id)
    }

    //获取图片url
    private fun loadImage(id: Int) {
        musicApi.fetchSongInfo(id.toString()).enqueue(object : Callback<MusicInfo> {
            override fun onFailure(call: Call<MusicInfo>, t: Throwable) {
                t.printStackTrace()
            }
            override fun onResponse(call: Call<MusicInfo>, response: Response<MusicInfo>) {
                response.body()?.let {
                    musicName = it.songs[0].name
                    musicArtist = it.songs[0].ar[0].name
                    picUrl = it.songs[0].al.picUrl
                    fetchBitmap()
                }
            }
        })
    }

    //获取位图并显示
    private fun fetchBitmap() {
        thread {
            //Glide加载图片
            picImg = Glide.with(this@MusicPlayActivity)
                    .asBitmap()
                    .load(picUrl)
                    .submit()
                    .get()
            runOnUiThread {
                //唱盘图片
                recordView.bm = picImg
                recordView.invalidate()
                //背景虚化图片
                Glide.with(this@MusicPlayActivity)
                        .load(picImg)
                        .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 30)))
                        .into(back_img)
            }
        }
    }

    //加载音乐源
    private fun loadData(id: Int) {
        musicApi.fetchSongUrl(id.toString()).enqueue(object : Callback<MusicUrlData> {
            override fun onFailure(call: Call<MusicUrlData>, t: Throwable) {
                t.printStackTrace()
            }
            override fun onResponse(call: Call<MusicUrlData>, response: Response<MusicUrlData>) {
                response.body()?.let {
                    if (it.code == 200) {
                        musicSource = it.data[0].url
                        //重置mp
                        mp.reset()
                        mp.setDataSource(musicSource)
                        mp.prepareAsync()
                    }
                }
            }
        })
    }

}