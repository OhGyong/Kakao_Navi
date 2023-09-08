package com.vcudemo.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RetrofitInstance {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(com.vcudemo.BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Singleton
    @Provides
    fun getDistanceService(): NavigationService = retrofit.create(NavigationService::class.java)
}