package com.viet.myaudioplayer.model.musicbykey

import com.google.gson.annotations.SerializedName
import com.viet.myaudioplayer.model.top.Artists

data class MusicByKey(
    var id: String,
    var name: String,
    var title: String,
    var code: String,
    var artists: List<Artists>,
    @SerializedName("artists_names")
    var artistsNames: String,
    var type: String,
    var thumbnail: String,
    var duration: Int,
    var source: Source
)