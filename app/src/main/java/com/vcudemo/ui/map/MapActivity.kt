package com.vcudemo.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapActivity: BaseActivity(), OnMapReadyCallback {
    companion object {
        const val TAG = "MapActivity"
    }

    private lateinit var binding: ActivityMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var naverMap: NaverMap

    // 내 위치(출발지) 좌표
    private var myLatitude = 0.0
    private var myLongitude = 0.0

    // 도착지 좌표
    private var endLatitude = 0.0
    private var endLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(com.vcudemo.BuildConfig.NAVER_CLIENT_ID)

        getMyLocation()
        onClickListener()
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady()")

        this.naverMap = naverMap

        // 현재 위치 버튼 on
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 카메라 줌 레벨 및 카메라 이동
        val cameraPosition = CameraPosition(LatLng(myLatitude, myLongitude), 13.0)
        naverMap.cameraPosition = cameraPosition
    }

    /**
     * 현재 위치 계산
     * todo : 위치 권한 체크 필요
     */
    @SuppressLint("MissingPermission")
    private fun getMyLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.d(TAG, "내 위치: $location")
                if(location?.latitude != null) {
                    myLatitude = location.latitude
                    myLongitude = location.longitude

                    binding.mapView.getMapAsync(this)
                }
            }
    }

    private fun onClickListener() {
        binding.etSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("latitude", myLatitude.toString())
            intent.putExtra("longitude", myLongitude.toString())
            getSearchResult.launch(intent)
        }

        /**
         * 맵에 포커스 뺏기 위한 조치
         */
        binding.ctDetail.setOnClickListener {
        }

        binding.btnIntentNavi.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            intent.putExtra("startLatitude", myLatitude)
            intent.putExtra("startLongitude", myLongitude)
            intent.putExtra("destinationLatitude", myLatitude)
            intent.putExtra("destinationLongitude", endLongitude)
            startActivity(intent)
            finish()
        }
    }

    private val getSearchResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK) {
                if(result.data != null) {
                    // 목적지 상세 View 노출
                    binding.etSearch.setText(result.data!!.getStringExtra("placeName"))
                    binding.tvPlaceName.text = result.data!!.getStringExtra("placeName")
                    binding.tvAddressName.text = result.data!!.getStringExtra("addressName")
                    binding.tvDistance.text = result.data!!.getStringExtra("distance")
                    binding.ctDetail.visibility = View.VISIBLE

                    endLatitude = result.data!!.getDoubleExtra("latitude", 0.0)
                    endLongitude = result.data!!.getDoubleExtra("longitude", 0.0)
                }

                // 목적지 마커 설정
                val marker = Marker()
                marker.width = 70
                marker.height = 100
                marker.position = LatLng(endLatitude, endLongitude)
                marker.map = naverMap

                // 카메라 이동
                val cameraPosition = CameraPosition(LatLng(endLatitude, endLongitude), 13.0)
                naverMap.cameraPosition = cameraPosition

            }else {
                // todo : 그냥 돌아왔을 때 처리하게 있나?
            }
        }
}