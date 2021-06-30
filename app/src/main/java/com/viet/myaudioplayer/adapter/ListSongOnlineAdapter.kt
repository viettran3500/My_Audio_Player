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
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.viewmodel.SongViewModel
import com.viet.myaudioplayer.activity.MainActivity
import com.viet.myaudioplayer.activity.PlayerActivity
import com.viet.myaudioplayer.model.SongInfo

class ListSongOnlineAdapter(
    private var mContext: Context,
    private var listSong: MutableList<SongInfo>,
    songViewModel: SongViewModel
): RecyclerView.Adapter<ListSongOnlineAdapter.ViewHolder>() {

    var sViewModel = songViewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.music_item_online,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        if(checkFavorite(position)){
            holder.imgLove.setBackgroundResource(R.drawable.ic_baseline_favorite_true)
        }else{
            holder.imgLove.setBackgroundResource(R.drawable.ic_baseline_favorite)
        }

        Glide.with(mContext)
            .load(listSong[position]?.thumbnail)
            .placeholder(R.drawable.ic_baseline_image_24)
            .error(R.drawable.ic_baseline_error_24)
            .into(holder.imgThumbnail)

        holder.tvTitle.text = listSong[position]!!.title
        holder.tvArtistsNames.text = listSong[position]!!.artistsNames
        holder.tvTime.text = formattedTime(listSong[position]!!.duration)
        holder.tvGenre.text = listSong[position].genre
        holder.imgLove.setOnClickListener {

            if(checkFavorite(position)){
                holder.imgLove.setBackgroundResource(R.drawable.ic_baseline_favorite)
                sViewModel.deleteSong(listSong[position])
            }else{
                holder.imgLove.setBackgroundResource(R.drawable.ic_baseline_favorite_true)
                sViewModel.insertSong(listSong[position])
            }
            notifyItemChanged(position)
        }

        holder.imgDownload.setOnClickListener {
            var request: DownloadManager.Request = DownloadManager.Request(Uri.parse(listSong[position].source))
            request.setTitle(Uri.parse(listSong[position].title).toString())
            request.setDescription("Download file...")
            request.setMimeType("audio/MP3")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "${listSong[position].title}.mp3")

            var downloadManager: DownloadManager = mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, PlayerActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("sender", "topSong")
            mContext.startActivity(intent)
        }

    }

    private fun checkFavorite(position: Int): Boolean {
        for (i in 0 until MainActivity.listMusicFavorite.size){
            if(listSong[position].id == MainActivity.listMusicFavorite[i].id)
                return true
        }
        return false
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
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
}