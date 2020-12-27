package com.example.steepmesic.pojo.ml

data class Album(
    val artist: Artist,
    val copyrightId: Int,
    val id: Int,
    val mark: Int,
    val name: String,
    val picId: Long,
    val publishTime: Long,
    val size: Int,
    val status: Int,
    val transNames: List<String>
)