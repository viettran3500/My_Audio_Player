package com.viet.myaudioplayer.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.viet.myaudioplayer.model.infomusic.DataInfo
import com.viet.myaudioplayer.model.musicbykey.DataMusicByKey
import com.viet.myaudioplayer.model.recommend.ItemRecommend
import com.viet.myaudioplayer.model.top.ItemTop
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    //http://mp3.zing.vn/xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1
    companion object{
        var gson: Gson
            get() = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create()
            set(value) = TODO()

        var apiService: ApiService
            get() = Retrofit.Builder()
                .baseUrl("http://mp3.zing.vn/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
            set(value) = TODO()

    }


    @GET("xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1")
    fun getTopSong(): Call<ItemTop>

    @GET("xhr/media/get-source?type=audio")
    fun getMusicByKey(@Query("key") key: String): Call<DataMusicByKey>

    @GET("xhr/media/get-info?type=audio")
    fun getInfoMusic(@Query("id") id: String): Call<DataInfo>

    @GET("xhr/recommend?type=audio")
    fun getSongRelated(@Query("id") id: String): Call<ItemRecommend>
}