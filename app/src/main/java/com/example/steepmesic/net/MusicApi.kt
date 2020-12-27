package com.example.steepmesic.net

import com.example.steepmesic.pojo.login.LoginResult
import com.example.steepmesic.pojo.minfo.MusicInfo
import com.example.steepmesic.pojo.ml.MusicSearchResult
import com.example.steepmesic.pojo.mrl.MusicRecommendResult
import com.example.steepmesic.pojo.murl.MusicUrlData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MusicApi {

    companion object {
        const val BASE_URL = "http://192.168.13.100:3000/"
    }

    //登录
    @GET("/login/cellphone")
    fun login(@Query("phone") phone: String,
              @Query("password") password: String)
            : Call<LoginResult>

    //获取每日推荐歌曲
    @GET("/recommend/songs")
    fun fetchRecommendSongs(@Header("Cookie") cookie: String?)
            : Call<MusicRecommendResult>

    //根据名称搜索音乐
    @GET("/search")
    fun searchSongsByName(@Query("keywords") keywords: String)
            : Call<MusicSearchResult>

    //获取音乐详情
    @GET("/song/detail")
    fun fetchSongInfo(@Query("ids") id: String)
            : Call<MusicInfo>

    //获取音乐播放链接
    @GET("/song/url")
    fun fetchSongUrl(@Query("id") id: String)
            : Call<MusicUrlData>

}