package com.example.steepmesic.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.steepmesic.R
import com.example.steepmesic.adapter.SongRecommendAdapter
import com.example.steepmesic.net.MusicApi
import com.example.steepmesic.pojo.mrl.DailySong
import com.example.steepmesic.pojo.mrl.MusicRecommendResult
import com.example.steepmesic.util.NetUtil
import kotlinx.android.synthetic.main.activity_music_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MusicListActivity : BaseActivity() {

    private lateinit var musicApi: MusicApi
    private lateinit var recommendSongList: ArrayList<DailySong>
    private lateinit var adapter: SongRecommendAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_list)

        setSupportActionBar(music_list_toolbar)

        //初始化
        recommendSongList = ArrayList()
        adapter = SongRecommendAdapter(this, recommendSongList)
        layoutManager = LinearLayoutManager(this)
        music_recommend_rcv.adapter = adapter
        music_recommend_rcv.layoutManager = layoutManager
        musicApi = NetUtil.create(MusicApi::class.java)

        //载入数据
        music_refresher.setColorSchemeColors(Color.parseColor("#F44336"))
        fetchRecommendSongs()

        //刷新监听
        music_refresher.setOnRefreshListener {
            refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.music_list_top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    //获取每日推荐歌曲
    private fun fetchRecommendSongs() {
        music_refresher.isRefreshing = true
        //取出cookie
        val cookie = getSharedPreferences("application", Context.MODE_PRIVATE)
                .getString("cookie", null)
        musicApi.fetchRecommendSongs(cookie).enqueue(object : Callback<MusicRecommendResult> {
            override fun onFailure(call: Call<MusicRecommendResult>, t: Throwable) {
                t.printStackTrace()
            }
            override fun onResponse(
                call: Call<MusicRecommendResult>,
                response: Response<MusicRecommendResult>
            ) {
                response.body()?.let {
                    recommendSongList.addAll(it.data.dailySongs)
                    adapter.notifyDataSetChanged()
                    music_refresher.isRefreshing = false
                }
            }
        })
    }

    private fun refresh() = fetchRecommendSongs()

}