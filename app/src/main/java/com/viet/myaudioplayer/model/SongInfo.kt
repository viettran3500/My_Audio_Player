package com.viet.myaudioplayer.model

import android.content.Context
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.Glide
import com.google.gson.annotations.SerializedName
import com.viet.myaudioplayer.R
import com.viet.myaudioplayer.activity.MainActivity

@Entity(tableName = "song")
data class SongInfo(
    @PrimaryKey
    val id: String,
    val title: String,
    @SerializedName("artists_names")
    val artistsNames: String,
    val thumbnail: String?,
    val duration: Int,
    var genre: String?,
    var source: String?
) {

    fun getDuration(duration: Int): String {
        val totalOut: String
        val totalNew: String
        val seconds: String = (duration % 60).toString()
        val minutes: String = (duration / 60).toString()
        totalOut = "$minutes:$seconds"
        totalNew = "$minutes:0$seconds"
        return if (seconds.length == 1) {
            totalNew
        } else {
            totalOut
        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter("loadImage")
        fun loadImage(imageView: ImageView, url: String) {
            Glide.with(imageView)
                .load(url)
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_error_24)
                .into(imageView)
        }

        @JvmStatic
        @BindingAdapter("loadImgFavorite")
        fun loadImgFavorite(imageView: ImageView, id: String) {
            if (checkFavorite(id)) {
                imageView.setBackgroundResource(R.drawable.ic_baseline_favorite_true)
            } else {
                imageView.setBackgroundResource(R.drawable.ic_baseline_favorite)
            }
        }

        private fun checkFavorite(id: String): Boolean {
            for (i in 0 until MainActivity.listMusicFavorite.size) {
                if (id == MainActivity.listMusicFavorite[i].id)
                    return true
            }
            return false
        }
    }
}