package com.shuklansh.templateapp.data.remote

import com.shuklansh.flaskvideoplayertheme.BuildConfig
import com.shuklansh.templateapp.data.apiResponseItem
import retrofit2.http.GET

interface flaskVideoApi {

    @GET("/allvideolist")
    suspend fun getApiData() : List<apiResponseItem>

    companion object{
        const val BASE_URL = BuildConfig.LINK
    }

}