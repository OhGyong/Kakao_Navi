package com.vcudemo.ui.map

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.vcudemo.R
import com.vcudemo.base.BaseActivity
import com.vcudemo.databinding.ActivityMapBinding

class MapActivity: BaseActivity(), OnMapReadyCallback {
    companion object {
        const val TAG = "MapActivity"
    }

    private lateinit var binding: ActivityMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var myLatitude = 0.0
    private var myLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(com.vcudemo.BuildConfig.NAVER_CLIENT_ID)

        getMyLocation()
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady()")

        // 카메라 줌 레벨 및 카메라 이동
        val cameraPosition = CameraPosition(LatLng(myLatitude, myLongitude), 13.0)
        naverMap.cameraPosition = cameraPosition

        // 현재 위치 마커
        val marker = Marker()
        marker.position = LatLng(myLatitude, myLongitude)
        marker.width = 40
        marker.height = 70
        marker.map = naverMap
    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.d(TAG, "내 위치: $location")
                if(location?.latitude != null) {
                    myLatitude = location.latitude
                    myLongitude = location.longitude

                    // todo : 지도 객체를 위치 정보를 얻고 초기화를 할지, 위치 정보를 onMapReady에서 얻을 지
                    binding.mapView.getMapAsync(this)
                }
            }
    }
}