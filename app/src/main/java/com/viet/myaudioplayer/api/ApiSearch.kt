package com.viet.myaudioplayer.api

import com.viet.myaudioplayer.model.search.Root
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiSearch {

    companion object{
        var apiSearch: ApiSearch
            get() = Retrofit.Builder()
                .baseUrl("http://ac.mp3.zing.vn/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiSearch::class.java)
            set(value) = TODO()
    }

    @GET("complete?type=artist,song,key,code&num=500")
    fun searchSong(@Query("query") query: String): Call<Root>
}