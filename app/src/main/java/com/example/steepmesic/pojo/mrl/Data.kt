package com.example.steepmesic.pojo.mrl

data class Data(
    val dailySongs: List<DailySong>,
    val orderSongs: List<Any>,
    val recommendReasons: List<RecommendReason>
)