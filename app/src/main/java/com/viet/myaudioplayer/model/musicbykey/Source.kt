package com.viet.myaudioplayer.model.musicbykey

import com.google.gson.annotations.SerializedName

data class Source(
    @SerializedName("128")
    var m128: String?,
    @SerializedName("320")
    var m320: String?)