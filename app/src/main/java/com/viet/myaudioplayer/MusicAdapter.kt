package com.viet.myaudioplayer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MusicAdapter(var mContext: Context, var mFile: MutableList<MusicFiles>): RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mFile.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fileName.text = mFile[position].title
        var image: ByteArray? = getAlbumArt(mFile[position].path)
        if(image != null){
            holder.albumArt.setImageBitmap(BitmapFactory.decodeByteArray(image,0,image.size))
        }
        else{
            holder.albumArt.setImageResource(R.drawable.music)
        }
        holder.itemView.setOnClickListener {
            val intent: Intent = Intent(mContext, PlayerActivity::class.java)
            intent.putExtra("position",position)
            mContext.startActivity(intent)
        }
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var fileName: TextView = view.findViewById(R.id.tvMusicFileName)
        var albumArt: ImageView = view.findViewById(R.id.imgMusic)
    }

    private fun getAlbumArt(uri: String): ByteArray? {
        var retriever: MediaMetadataRetriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        var art: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return art
    }
}