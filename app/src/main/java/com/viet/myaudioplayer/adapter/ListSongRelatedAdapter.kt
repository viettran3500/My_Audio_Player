package com.viet.myaudioplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.OnItemClickListener
import com.viet.myaudioplayer.databinding.MusicItemOnlineBinding
import com.viet.myaudioplayer.model.SongInfo

class ListSongRelatedAdapter(
    private var mContext: Context,
    private var listener: OnItemClickListener
) : RecyclerView.Adapter<ListSongRelatedAdapter.ViewHolder>() {

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

            binding.imgLove.visibility = View.GONE

            binding.imgDownload.visibility = View.GONE

            binding.audioItem.setOnClickListener {
                listener.onItemClick(position)
            }
        }
    }

    fun setListSong(songs: MutableList<SongInfo>) {
        this.listSong = songs
        notifyDataSetChanged()
    }
}