package com.shuklansh.templateapp.di

import com.shuklansh.templateapp.data.remote.flaskVideoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun getApi(): flaskVideoApi {
        return Retrofit.Builder().baseUrl(flaskVideoApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(flaskVideoApi::class.java)
    }

}