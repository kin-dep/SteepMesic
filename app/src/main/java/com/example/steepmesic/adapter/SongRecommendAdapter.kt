package com.example.steepmesic.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.steepmesic.R
import com.example.steepmesic.activity.MusicPlayActivity
import com.example.steepmesic.pojo.mrl.DailySong

class SongRecommendAdapter(val context: Context, val recommendSongList: List<DailySong>)
    : RecyclerView.Adapter<SongRecommendAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val musicNameTv: TextView = v.findViewById(R.id.music_name)
        val musicArtistAlbumTv: TextView = v.findViewById(R.id.music_artist_album)
        val recommendReasonTv: TextView = v.findViewById(R.id.recommend_reason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommend_music, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = recommendSongList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            musicNameTv.text = recommendSongList[position].name
            musicArtistAlbumTv.text = "${recommendSongList[position].ar[0].name} - ${recommendSongList[position].al.name}"
            recommendReasonTv.text = recommendSongList[position].reason
            itemView.setOnClickListener {
                val intent = Intent(context, MusicPlayActivity::class.java)
                //传递歌曲播放列表(此列表视图)和选中位置
                val musicIds = ArrayList<Int>()
                for (song in recommendSongList) musicIds.add(song.id)
                intent.putExtra("musicIds", musicIds)
                intent.putExtra("pos", position)
                context.startActivity(intent)
            }
        }
    }

}