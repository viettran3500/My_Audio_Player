package com.viet.myaudioplayer.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.activity.AlbumDetails
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.model.MusicFiles
import java.lang.Exception

class AlbumAdapter(private var mContext: Context, private var mFile: MutableList<MusicFiles>) :
    RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var albumName: TextView = view.findViewById(R.id.tvAlbumName)
        var albumImage: ImageView = view.findViewById(R.id.imgAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mFile.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.albumName.text = mFile[position].album
        val image: Bitmap? = getAlbumArt(mFile[position].path)
        if (image != null) {
            holder.albumImage.setImageBitmap(image)
        } else {
            holder.albumImage.setImageResource(R.drawable.music)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, AlbumDetails::class.java)
            intent.putExtra("albumName", mFile[position].album)
            mContext.startActivity(intent)
        }
    }

    private fun getAlbumArt(uri: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(mContext, Uri.parse(uri))
        val art: ByteArray? = retriever.embeddedPicture
        return if (art != null) {
            BitmapFactory.decodeByteArray(art, 0, art.size)
        } else {
            null
        }

    }
}