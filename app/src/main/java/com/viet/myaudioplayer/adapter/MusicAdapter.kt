package com.viet.myaudioplayer.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.viet.myaudioplayer.activity.PlayerActivity
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.model.MusicFiles
import java.lang.Exception

class MusicAdapter(private var mContext: Context, private var mFile: MutableList<MusicFiles>) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mFile.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fileName.text = mFile[position].title
        val image: Bitmap? = getAlbumArt(mFile[position].albumID)
        if (image != null) {
            holder.albumArt.setImageBitmap(image)
        } else {
            holder.albumArt.setImageResource(R.drawable.music)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, PlayerActivity::class.java)
            intent.putExtra("position", position)
            mContext.startActivity(intent)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var fileName: TextView = view.findViewById(R.id.tvMusicFileName)
        var albumArt: ImageView = view.findViewById(R.id.imgMusic)
    }

    private fun getAlbumArt(uri: String): Bitmap? {
        return try {
            val pfd: ParcelFileDescriptor? =
                mContext.contentResolver.openFileDescriptor(Uri.parse(uri), "r")
            val fileDescriptor = pfd!!.fileDescriptor
            BitmapFactory.decodeFileDescriptor(fileDescriptor)
        } catch (e: Exception) {
            null
        }

    }
}