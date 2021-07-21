package com.viet.myaudioplayer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.activity.PlayerActivity
import com.viet.myaudioplayer.databinding.MusicItemsBinding
import com.viet.myaudioplayer.model.MusicFiles

class MusicAdapter(private var mContext: Context) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    private var mFile: MutableList<MusicFiles> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: MusicItemsBinding =
            MusicItemsBinding.inflate(layoutInflater, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mFile.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(position)
    }

    inner class ViewHolder(var binding: MusicItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val data: MusicFiles = mFile[position]
            binding.dataOffline = data
            binding.executePendingBindings()

            binding.audioItem.setOnClickListener {
                val intent = Intent(mContext, PlayerActivity::class.java)
                intent.putExtra("position", position)
                mContext.startActivity(intent)
            }
        }
    }

    fun setList(mFile: MutableList<MusicFiles>) {
        this.mFile = mFile
        notifyDataSetChanged()
    }
}