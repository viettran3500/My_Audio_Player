package com.viet.myaudioplayer.model.search

//data class SearchSong(
//    var thumb: String,
//    var artist: String,
//    var duration: Int,
//    var name: String,
//    var id: String
//)

data class Song(
    var hasVideo: String?,
    var thumb: String?,
    var artist: String?,
    var streamingStatus: String?,
    var thumbVideo: String?,
    var genreIds: String?,
    var disable_platform_web: String?,
    var artistIds: String?,
    var disSPlatform: String?,
    var duration: String?,
    var radioPid: String?,
    var zing_choice: String?,
    var name: String?,
    var block: String?,
    var id: String?,
    var disDPlatform: String?
)