package com.viet.myaudioplayer.model.recommend

import com.viet.myaudioplayer.model.top.Songs

data class ItemRecommend(
    var err: Int,
    var msg: String,
    var data: ListSongRecommend
)