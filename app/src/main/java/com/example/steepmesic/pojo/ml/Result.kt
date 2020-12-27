package com.example.steepmesic.pojo.ml

data class Result(
    val hasMore: Boolean,
    val songCount: Int,
    val songs: List<Song>
)