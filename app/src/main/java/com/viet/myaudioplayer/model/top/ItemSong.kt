package com.viet.myaudioplayer.model.top

import com.google.gson.annotations.SerializedName

data class ItemSong(
    var id: String,
    var name: String,
    var title: String,
    var code: String,
    var artists: List<Artists>,
    @SerializedName("artists_names")
    var artistsNames: String,
    var type: String,
    var thumbnail: String,
    var duration: Int
)