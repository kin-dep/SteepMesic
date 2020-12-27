package com.example.steepmesic.pojo.minfo

data class MusicInfo(
    val code: Int,
    val privileges: List<Privilege>,
    val songs: List<Song>
)