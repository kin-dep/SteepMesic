package com.example.steepmesic.activity

import android.content.*
import android.graphics.*
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.steepmesic.R
import com.example.steepmesic.net.MusicApi
import com.example.steepmesic.pojo.minfo.MusicInfo
import com.example.steepmesic.pojo.murl.MusicUrlData
import com.example.steepmesic.receiver.MusicReceiver
import com.example.steepmesic.service.MusicService
import com.example.steepmesic.util.NetUtil
import com.example.steepmesic.view.RecordView
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_music_play.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.thread

class MusicPlayActivity : BaseActivity(), View.OnClickListener {

    //注册广播接收器
    private val mReceiver = MusicReceiver()

    //通过binder与service通信
    private var mBinder: MusicService.MusicBinder? = null

    private val conn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mBinder = null
        }
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mBinder = service as MusicService.MusicBinder
            //判断mBinder持有图片才更新界面
            mBinder?.let {
                if (it.isHolding) {
                    showMusicTextInfo()
                    showMusicImgInfo()
                    showMusicSourceInfo()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_play)


//        Log.d("create musicActivity", "enter")

        //初始颜色调暗
        back_img.setBackgroundColor(Color.parseColor("#333333"))

        //绑定service
        initService()

        //监听按键点击
        btn_play_pause.setOnClickListener(this)
        btn_next.setOnClickListener(this)
        btn_prev.setOnClickListener(this)
        btn_back.setOnClickListener(this)

        //进度条拖动
        music_progressbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?,
                                           progress: Int,
                                           fromUser: Boolean) {
                if (fromUser) { mBinder?.mProgress = progress }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        //注册接收器
        val intentFilter = IntentFilter().apply {
            addAction(MusicReceiver.UPDATE_MUSIC_TEXT)
            addAction(MusicReceiver.UPDATE_MUSIC_IMG)
            addAction(MusicReceiver.UPDATE_MUSIC_SOURCE)
            addAction(MusicReceiver.UPDATE_PLAY_PAUSE_STATE)
        }
        registerReceiver(mReceiver, intentFilter)

    }

    override fun onBackPressed() { finish() }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    private fun initService() {
        /*val musicIds = intent.getIntegerArrayListExtra("musicIds")
        val pos = intent.getIntExtra("pos", -1)
        val intent = Intent(this, MusicService::class.java).apply {
            putExtra(MusicService.COMMAND, MusicService.COMMAND_LOAD)
            putExtra("musicIds", musicIds)
            putExtra("pos", pos)
        }
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
        startService(intent)*/

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, conn, Context.BIND_EXTERNAL_SERVICE)
    }

    //将时间格式化
    private fun convertTime(time: Int): String {
        val sec = time / 1000
        val currentMinute = String.format("%02d", sec / 60)
        val currentSecond = String.format("%02d", sec % 60)
        return "$currentMinute:$currentSecond"
    }

    //展示音乐的文本信息
    fun showMusicTextInfo() {
        mBinder?.let {
            music_title.text = it.mName
            music_artist.text = it.mArtist
        }
    }

    //展示音乐的图片信息
    fun showMusicImgInfo() {
        mBinder?.let {
            //唱盘图片
            recordView.bm = it.mImage
            recordView.invalidate()
            //背景虚化图片
            Glide.with(this)
                    .load(it.mImage)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(20, 20)))
                    .into(back_img)
        }
    }

    //展示音乐的数据源信息，并开启线程动态加载音乐进度
    fun showMusicSourceInfo() {
        mBinder?.let {
            total_time.text = convertTime(it.mDuration!!)
            current_time.text = "00:00"
            music_progressbar.max = it.mDuration!!
            btn_play_pause.setImageResource(R.drawable.ic_pause)
            thread {
                while (true) {
                    Thread.sleep(1000)
                    runOnUiThread {
                        music_progressbar.progress = it.mProgress
                        current_time.text = convertTime(it.mProgress)
                    }
                }
            }
            startDiscAnimation()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btn_play_pause -> {
                if (mBinder?.isPlaying!!) {
                    deal(MusicService.COMMAND_PAUSE)
                    changePlayPause(R.drawable.ic_play)
                } else {
                    deal(MusicService.COMMAND_PLAY)
                    changePlayPause(R.drawable.ic_pause)
                }
            }
            btn_prev -> deal(MusicService.COMMAND_PREV)
            btn_next -> deal(MusicService.COMMAND_NEXT)
            btn_back -> finish()
        }
    }

    private fun deal(op: String) {
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra(MusicService.COMMAND, op)
        startService(intent)
    }

    fun changePlayPause(resId: Int) {
        btn_play_pause.setImageResource(resId)
        startDiscAnimation()
    }

    private fun startDiscAnimation() {
        mBinder?.let {
            thread {
                recordView.isPlaying = it.isPlaying
                while (recordView.isPlaying || recordView.needleRadiusCounter > RecordView.PAUSE_DEGREE) {
                    recordView.invalidate()
                }
            }
        }
    }

}