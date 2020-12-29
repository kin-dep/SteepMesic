package com.example.steepmesic.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.steepmesic.R
import com.example.steepmesic.activity.MusicPlayActivity
import com.example.steepmesic.pojo.ml.Song
import com.example.steepmesic.service.MusicService
import java.util.regex.Pattern

/**
 * 歌曲搜索后的界面展示适配器
 */
class SongPosterAdapter(val context: Context,
                        val songList: List<Song>,
                        var keywords: String? = null)
    : RecyclerView.Adapter<SongPosterAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val musicNameTv: TextView = v.findViewById(R.id.music_name)
        val musicArtistAlbumTv: TextView = v.findViewById(R.id.music_artist_album)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_music, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = songList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            //关键词标红
            val spanMusicName = SpannableString(songList[position].name)
            val spanArtistAlbum = SpannableString("${songList[position].artists[0].name} - ${songList[position].album.name}")
            keywords?.let {
                var keyIdx: Int
                keyIdx = spanMusicName.indexOf(keywords!!, ignoreCase = true)
                if (keyIdx != -1) {
                    spanMusicName.setSpan(ForegroundColorSpan(Color.RED), keyIdx,
                            keyIdx + keywords!!.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                keyIdx = spanArtistAlbum.indexOf(keywords!!, ignoreCase = true)
                if (keyIdx != -1) {
                    spanArtistAlbum.setSpan(ForegroundColorSpan(Color.RED), keyIdx,
                            keyIdx + keywords!!.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            musicArtistAlbumTv.text = spanArtistAlbum
            musicNameTv.text = spanMusicName
            itemView.setOnClickListener {
                /*val intent = Intent(context, MusicPlayActivity::class.java)
                //传递歌曲播放列表(此列表视图)和选中位置
                val musicIds = ArrayList<Int>()
                for (song in songList) musicIds.add(song.id)
                intent.putExtra("musicIds", musicIds)
                intent.putExtra("pos", position)
                context.startActivity(intent)*/

                //传递歌曲播放列表(此列表视图)和选中位置
                val musicIds = ArrayList<Int>()
                for (song in songList) musicIds.add(song.id)
                val intent = Intent(context, MusicService::class.java).apply {
                    putExtra("musicIds", musicIds)
                    putExtra("pos", position)
                    putExtra(MusicService.COMMAND, MusicService.COMMAND_LOAD)
                }
                //开启服务和播放的activity
                context.startService(intent)
                context.startActivity(Intent(context, MusicPlayActivity::class.java))
            }
        }
    }

}