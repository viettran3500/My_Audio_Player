package com.viet.myaudioplayer.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "song")
data class SongInfo(
    @PrimaryKey
    var id: String,
    var title: String,
    @SerializedName("artists_names")
    var artistsNames: String,
    var thumbnail: String?,
    var duration: Int,
    var genre: String?,
    var source: String?
)