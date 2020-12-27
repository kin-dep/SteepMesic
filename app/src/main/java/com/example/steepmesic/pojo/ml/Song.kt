package com.example.steepmesic.pojo.ml

import com.example.steepmesic.pojo.ml.Album
import com.example.steepmesic.pojo.ml.ArtistX

data class Song(
    val album: Album,
    val alias: List<String>,
    val artists: List<ArtistX>,
    val copyrightId: Int,
    val duration: Int,
    val fee: Int,
    val ftype: Int,
    val id: Int,
    val mark: String,
    val mvid: Int,
    val name: String,
    val rUrl: Any,
    val rtype: Int,
    val status: Int,
    val transNames: List<String>
)