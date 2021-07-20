package com.viet.myaudioplayer.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.viet.myaudioplayer.R

data class MusicFiles(
    val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumID: String,
    val duration: String
) {

    companion object {
        @JvmStatic
        @BindingAdapter("loadImageOff")
        fun getAlbumArt(imageView: ImageView, uri: String) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(imageView.context, Uri.parse(uri))
            val art: ByteArray? = retriever.embeddedPicture
            if (art != null) {
                val image = BitmapFactory.decodeByteArray(art, 0, art.size)
                imageView.setImageBitmap(image)
            } else {
                imageView.setImageResource(R.drawable.music)
            }
        }
    }
}