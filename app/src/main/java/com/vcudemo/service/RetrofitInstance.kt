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
    private val sKRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(com.vcudemo.BuildConfig.SK_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val kakaoRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(com.vcudemo.BuildConfig.KAKAO_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Singleton
    @Provides
    fun navigationService(): NavigationService = sKRetrofit.create(NavigationService::class.java)

    @Singleton
    @Provides
    fun mapService(): MapService = kakaoRetrofit.create(MapService::class.java)
}