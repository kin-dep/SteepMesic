package com.example.steepmesic.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.steepmesic.R
import com.example.steepmesic.activity.MusicPlayActivity
import com.example.steepmesic.net.MusicApi
import com.example.steepmesic.pojo.minfo.MusicInfo
import com.example.steepmesic.pojo.murl.MusicUrlData
import com.example.steepmesic.receiver.MusicReceiver
import com.example.steepmesic.receiver.MusicReceiver.Companion.MUSIC_NOTIFIED_NEXT
import com.example.steepmesic.receiver.MusicReceiver.Companion.MUSIC_NOTIFIED_PLAY
import com.example.steepmesic.receiver.MusicReceiver.Companion.MUSIC_NOTIFIED_PREV
import com.example.steepmesic.receiver.MusicReceiver.Companion.MUSIC_NOTIFIED_STOP
import com.example.steepmesic.receiver.MusicReceiver.Companion.TARGET_FLAG
import com.example.steepmesic.util.NetUtil
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_music_play.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList
import kotlin.concurrent.thread

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "com.example.music.service.music"
        const val COMMAND = "command"
        const val COMMAND_LOAD = "load"
        const val COMMAND_PLAY = "play"
        const val COMMAND_PAUSE = "pause"
        const val COMMAND_RESTART = "restart"
        const val COMMAND_NEXT = "next"
        const val COMMAND_PREV = "prev"
    }

    //在MusicService中设置MediaPlayer
    private val mPlayer = MediaPlayer()
    private val mReceiver = MusicReceiver()

    //音乐请求API
    private lateinit var musicApi: MusicApi
    //音乐播放列表
    private lateinit var musicIds: ArrayList<Int>
    //当前播放位置
    private var pos: Int = -1
    //歌曲信息
    private var id = -1 //歌曲id
    private var musicName = "unknown"
    private var musicArtist = "unknown"
    private var musicAlbum = "unknown"
    private lateinit var picUrl: String
    private var picImg: Bitmap? = null
    private lateinit var musicSource: String

    private val serviceView by lazy {
        RemoteViews(packageName, R.layout.layout_notification).apply {
            setTextViewText(R.id.music_name, musicName)
            setTextViewText(R.id.music_artist_album, "$musicArtist - $musicAlbum")
            //设置通知处的按钮点击事件，发送广播
            val intentPrev = Intent(MusicReceiver.UPDATE_UI_BROADCAST).putExtra(TARGET_FLAG, MUSIC_NOTIFIED_PREV)
            val intentNext = Intent(MusicReceiver.UPDATE_UI_BROADCAST).putExtra(TARGET_FLAG, MUSIC_NOTIFIED_NEXT)
            val intentPlay = Intent(MusicReceiver.UPDATE_UI_BROADCAST).putExtra(TARGET_FLAG, MUSIC_NOTIFIED_PLAY)
            val intentStop = Intent(MusicReceiver.UPDATE_UI_BROADCAST).putExtra(TARGET_FLAG, MUSIC_NOTIFIED_STOP)
            //***requestCode必须从0开始顺序编号
            setOnClickPendingIntent(R.id.btn_prev, PendingIntent.getBroadcast(this@MusicService, 0, intentPrev, 0))
            setOnClickPendingIntent(R.id.btn_next, PendingIntent.getBroadcast(this@MusicService, 1, intentNext, 0))
            setOnClickPendingIntent(R.id.btn_play, PendingIntent.getBroadcast(this@MusicService, 2, intentPlay, 0))
            setOnClickPendingIntent(R.id.btn_pause, PendingIntent.getBroadcast(this@MusicService, 3, intentStop, 0))
        }
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val builder by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "我的音乐前台服务", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
            NotificationCompat.Builder(this, CHANNEL_ID)
        } else {
            NotificationCompat.Builder(this)
        }
    }

    private val notification by lazy {
        val intent = Intent(this, MusicPlayActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, 0)
        builder.setSmallIcon(R.drawable.ic_music)
                .setCustomContentView(serviceView)
                .setNotificationSilent()
                .setContentIntent(pi)
                .setOngoing(true)
                .build()
    }

    private val mBinder by lazy {
        MusicBinder()
    }

    //声明使用inner才能直接访问外部类
    //通过binder传输数据
    inner class MusicBinder : Binder() {
        var isHolding = false
            get() = field
            set(value) {field = value}
        val mImage: Bitmap?
            get() = picImg
        val mName: String
            get() = musicName
        val mArtist
            get() = musicArtist
        val isPlaying
            get() = mPlayer.isPlaying
        val mDuration
            get() = mPlayer.duration
        var mProgress
            get() = mPlayer.currentPosition
            set(value) = mPlayer.seekTo(value)
    }

    //创建service
    override fun onCreate() {
        super.onCreate()

        //初始化
        musicApi = NetUtil.create(MusicApi::class.java)
        mPlayer.setOnPreparedListener {
            //加载数据源成功的广播
            sendBroadcast(Intent(MusicReceiver.UPDATE_MUSIC_SOURCE))
            it.start()
        }

        startForeground(1, notification)
        //注册receiver
        registerReceiver(mReceiver, IntentFilter(MusicReceiver.UPDATE_UI_BROADCAST))

    }

    override fun onDestroy() {
        Log.i("MusicService", "destroyed")
        unregisterReceiver(mReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.getStringExtra(COMMAND)) {
                COMMAND_LOAD -> {
                    //获取播放列表和当前位置然后加载
                    musicIds = intent.getIntegerArrayListExtra("musicIds")!!
                    pos = intent.getIntExtra("pos", -1)!!
                    if (pos != -1) {
                        id = musicIds[pos]
                        loadMusic(id)
                    }
                }
                COMMAND_PLAY -> play()
                COMMAND_PAUSE -> pause()
                COMMAND_NEXT -> {
                    Log.d("COMMAND_NEXT", "enter")
                    next()
                }
                COMMAND_PREV -> prev()
//                COMMAND_RESTART -> restart()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): Binder {
        return mBinder
    }

    //切换音乐的准备
    private fun switchMusic(cur: Int) {
        mPlayer.reset() //重置
        loadMusic(musicIds[cur])
    }

    fun prev() {
        Log.d("##########service prev", "enter")
        if (pos == 0) pos = musicIds.size
        switchMusic(--pos)
    }

    fun next() {
        Log.d("##########service next", "enter")
        pos = (pos + 1) % musicIds.size
        switchMusic(pos)
    }

//    fun restart() {
//        mPlayer.stop()
//        mPlayer.prepare()
//        mPlayer.start()
//    }

    fun pause() {
        if (mPlayer.isPlaying) {
            mPlayer.pause()
            //更新activity界面
            sendBroadcast(Intent(MusicReceiver.UPDATE_PLAY_PAUSE_STATE).apply {
                putExtra("destResId", R.drawable.ic_play)
            })
            //更新通知界面
            serviceView.setViewVisibility(R.id.btn_play, View.VISIBLE)
            serviceView.setViewVisibility(R.id.btn_pause, View.GONE)
            startForeground(1, notification)
        }
    }

    fun play() {
        if (!mPlayer.isPlaying) {
            mPlayer.start()
            //更新activity界面
            sendBroadcast(Intent(MusicReceiver.UPDATE_PLAY_PAUSE_STATE).apply {
                putExtra("destResId", R.drawable.ic_pause)
            })
            //更新通知界面
            serviceView.setViewVisibility(R.id.btn_play, View.GONE)
            serviceView.setViewVisibility(R.id.btn_pause, View.VISIBLE)
            startForeground(1, notification)
        }
        mPlayer.setOnCompletionListener { next() }
    }

    //停止服务
    fun stop() {
        pause()
        stopSelf()
    }

    //加载音乐源和图片
    private fun loadMusic(id: Int) {
        Log.d("##############cur id", "$id")
        sendBroadcast(Intent(MusicReceiver.UPDATE_PLAY_PAUSE_STATE).apply {
            putExtra("destResId", R.drawable.ic_play)
        })
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
                    musicAlbum = it.songs[0].al.name
                    //加载好信息后发送一条更新界面的广播，由activity接收
                    sendBroadcast(Intent(MusicReceiver.UPDATE_MUSIC_TEXT))
                    //更改通知界面
                    serviceView.apply {
                        setTextViewText(R.id.music_name, musicName)
                        setTextViewText(R.id.music_artist_album, "$musicArtist - $musicAlbum")
                    }
                    startForeground(1, notification)
                    //加载图片
                    picUrl = it.songs[0].al.picUrl
                    fetchBitmap(picUrl)
                }
            }
        })
    }

    //获取位图并显示
    private fun fetchBitmap(picUrl: String) {
        thread {
            //Glide加载图片
            picImg = Glide.with(this)
                    .asBitmap()
                    .load(picUrl)
                    .submit(270, 270)
                    .get()

            mBinder.isHolding = true //持有图片，可恢复

            sendBroadcast(Intent(MusicReceiver.UPDATE_MUSIC_IMG))
            //更改通知界面图片
            serviceView.setImageViewBitmap(R.id.music_image, picImg)
            startForeground(1, notification)
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
                        mPlayer.reset()
                        mPlayer.setDataSource(musicSource)
                        mPlayer.prepareAsync()
                    }
                }
            }
        })
    }

    //测试：与activty断开连接后停止
    override fun unbindService(conn: ServiceConnection) {
        stopSelf()
    }

}