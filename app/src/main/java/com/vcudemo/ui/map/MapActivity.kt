package com.vcudemo.ui.map

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.vcudemo.R
import com.vcudemo.base.BaseActivity
import com.vcudemo.databinding.ActivityMapBinding

class MapActivity: BaseActivity() {
    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)

        binding.mapView.start(object: MapLifeCycleCallback() {
            override fun onMapDestroy() {
                TODO("Not yet implemented")
            }

            override fun onMapError(error: Exception?) {
                TODO("Not yet implemented")
            }

        }, object: KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                println("!")
            }
        })
    }
}