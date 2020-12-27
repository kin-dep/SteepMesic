package com.example.steepmesic.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.steepmesic.R
import com.example.steepmesic.adapter.SongPosterAdapter
import com.example.steepmesic.net.MusicApi
import com.example.steepmesic.pojo.ml.MusicSearchResult
import com.example.steepmesic.pojo.ml.Song
import com.example.steepmesic.util.NetUtil
import kotlinx.android.synthetic.main.activity_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : BaseActivity() {

    private lateinit var musicApi: MusicApi
    private lateinit var songList: ArrayList<Song>
    private lateinit var adapter: SongPosterAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //初始化
        musicApi = NetUtil.create(MusicApi::class.java)
        songList = ArrayList()
        adapter = SongPosterAdapter(this, songList)
        layoutManager = LinearLayoutManager(this)
        music_search_rcv.adapter = adapter
        music_search_rcv.layoutManager = layoutManager


        //监听搜索点击事件
        search_input.setOnEditorActionListener { v, actionId, event ->
            if (actionId == IME_ACTION_SEARCH) {
                val keywords = search_input.text.toString()
                if (TextUtils.isEmpty(keywords)) {
                    Toast.makeText(this, "请输入歌曲名", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    searchMusicList(keywords)
                }
                true
            } else false
        }

        //监听取消事件
        cancel.setOnClickListener { finish() }
    }

    //异步搜索音乐
    private fun searchMusicList(keywords: String) {
        musicApi.searchSongsByName(keywords).enqueue(object : Callback<MusicSearchResult> {
            override fun onFailure(call: Call<MusicSearchResult>, t: Throwable) {
                t.printStackTrace()
            }
            override fun onResponse(
                call: Call<MusicSearchResult>,
                response: Response<MusicSearchResult>
            ) {
                response.body()?.let {
                    if (it.code == 200) {
                        songList.addAll(it.result.songs)
                        //匹配搜索的keywords变蓝效果
                        adapter.keywords = keywords
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

}