package com.viet.myaudioplayer.adapter

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.viet.myaudioplayer.OnItemClickListener
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.activity.PlayerActivity
import com.viet.myaudioplayer.model.SongInfo
import com.viet.myaudioplayer.viewmodel.SongViewModel

class ListSongRelatedAdapter(
    private var mContext: Context,
    private var listener: OnItemClickListener
): RecyclerView.Adapter<ListSongRelatedAdapter.ViewHolder>() {

    private var listSong: MutableList<SongInfo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.music_item_online,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(mContext)
            .load(listSong[position]?.thumbnail)
            .placeholder(R.drawable.ic_baseline_image_24)
            .error(R.drawable.ic_baseline_error_24)
            .into(holder.imgThumbnail)

        holder.tvTitle.text = listSong[position].title
        holder.tvArtistsNames.text = listSong[position].artistsNames
        holder.tvTime.text = formattedTime(listSong[position].duration)
        holder.tvGenre.text = listSong[position].genre
        holder.imgLove.visibility = View.INVISIBLE
        holder.imgDownload.visibility = View.INVISIBLE

        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }

    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var imgThumbnail: ImageView = view.findViewById(R.id.imgThumbnail)
        var tvTitle: TextView = view.findViewById(R.id.tvTitle)
        var tvArtistsNames: TextView = view.findViewById(R.id.tvArtistsNames)
        var tvGenre: TextView = view.findViewById(R.id.tvGenre)
        var tvTime: TextView = view.findViewById(R.id.tvTime)
        var imgLove: ImageButton = view.findViewById(R.id.imgLove)
        var imgDownload: ImageButton = view.findViewById(R.id.imgDownload)
    }

    private fun formattedTime(mCurrentPosition: Int): String {
        val totalOut: String
        val totalNew: String
        val seconds: String = (mCurrentPosition % 60).toString()
        val minutes: String = (mCurrentPosition / 60).toString()
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"
        return if (seconds.length == 1) {
            totalNew
        } else {
            totalOut
        }
    }

    fun setListSong(songs: MutableList<SongInfo>){
        this.listSong = songs
        notifyDataSetChanged()
    }
}