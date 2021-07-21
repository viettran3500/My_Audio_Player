package com.viet.myaudioplayer.adapter

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.viewmodel.SongViewModel
import com.viet.myaudioplayer.activity.PlayerActivity
import com.viet.myaudioplayer.databinding.MusicItemOnlineBinding
import com.viet.myaudioplayer.model.SongInfo

class ListSongSearchAdapter(
    private var mContext: Context,

    private var sViewModel: SongViewModel
) : RecyclerView.Adapter<ListSongSearchAdapter.ViewHolder>() {

    private var listSong: MutableList<SongInfo> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: MusicItemOnlineBinding =
            MusicItemOnlineBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(position)

    }

    inner class ViewHolder(private val binding: MusicItemOnlineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val data = listSong[position]
            binding.recyclerData = data
            binding.executePendingBindings()

            binding.imgLove.setOnClickListener {
                if (binding.imgLove.background.constantState?.equals(
                        ContextCompat.getDrawable(
                            mContext,
                            R.drawable.ic_baseline_favorite_true
                        )?.constantState
                    )!!
                ) {
                    sViewModel.deleteSong(data)
                } else {
                    sViewModel.insertSong(data)
                }
            }

            binding.imgDownload.setOnClickListener {
                val request: DownloadManager.Request =
                    DownloadManager.Request(Uri.parse(data.source))
                request.setTitle(Uri.parse(data.title).toString())
                request.setDescription("Download file...")
                request.setMimeType("audio/MP3")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_MUSIC,
                    "${data.title}.mp3"
                )

                val downloadManager: DownloadManager =
                    mContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                downloadManager.enqueue(request)
            }

            binding.audioItem.setOnClickListener {
                val intent = Intent(mContext, PlayerActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("sender", "searchSong")
                mContext.startActivity(intent)
            }
        }
    }

    fun setListSong(songs: MutableList<SongInfo>) {
        this.listSong = songs
        notifyDataSetChanged()
    }
}