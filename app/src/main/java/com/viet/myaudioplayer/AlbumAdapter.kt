package com.viet.myaudioplayer

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlbumAdapter(var mContext: Context, var mFile: MutableList<MusicFiles>): RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var albumName: TextView = view.findViewById(R.id.tvAlbumName)
        var albumImage: ImageView = view.findViewById(R.id.imgAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View = LayoutInflater.from(mContext).inflate(R.layout.album_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mFile.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.albumName.text = mFile[position].album
        var image: ByteArray? = getAlbumArt(mFile[position].path)
        if(image != null){
            holder.albumImage.setImageBitmap(BitmapFactory.decodeByteArray(image,0,image.size))
        }
        else{
            holder.albumImage.setImageResource(R.drawable.music)
        }

        holder.itemView.setOnClickListener {
            val intent: Intent = Intent(mContext, AlbumDetails::class.java)
            intent.putExtra("albumName",mFile[position].album)
            mContext.startActivity(intent)
        }
    }

    fun getAlbumArt(uri: String): ByteArray? {
        var retriever: MediaMetadataRetriever = MediaMetadataRetriever()
        retriever.setDataSource(uri)
        var art: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return art
    }
}