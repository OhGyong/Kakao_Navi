package com.vcudemo.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BaseService {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(com.vcudemo.BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}