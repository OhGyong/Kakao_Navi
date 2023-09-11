package com.vcudemo.ui.map

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.vcudemo.R
import com.vcudemo.base.BaseActivity
import com.vcudemo.databinding.ActivityMapBinding

class MapActivity: BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        println()
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(com.vcudemo.BuildConfig.NAVER_CLIENT_ID)
    }

    override fun onMapReady(naverMap: NaverMap) {

    }
}