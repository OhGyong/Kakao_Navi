package com.vcudemo.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import com.vcudemo.R
import com.vcudemo.base.BaseActivity
import com.vcudemo.databinding.ActivityMapBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapActivity: BaseActivity(), OnMapReadyCallback {
    companion object {
        const val TAG = "MapActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var binding: ActivityMapBinding
    private lateinit var viewModel: MapViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource

    private var myLatitude = 0.0
    private var myLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        viewModel = ViewModelProvider(this@MapActivity)[MapViewModel::class.java]
        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(com.vcudemo.BuildConfig.NAVER_CLIENT_ID)

        observeFlow()
        getMyLocation()

        binding.btnSearch.setOnClickListener {
            viewModel.getSearchPlaceData(myLatitude.toString(), myLongitude.toString())
        }

        binding.etSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("latitude", myLatitude.toString())
            intent.putExtra("longitude", myLongitude.toString())
            getSearchResult.launch(intent)
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady()")

        // 위치 추적 모드 on
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

        // 현재 위치 버튼 on
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 카메라 줌 레벨 및 카메라 이동
        val cameraPosition = CameraPosition(LatLng(myLatitude, myLongitude), 13.0)
        naverMap.cameraPosition = cameraPosition
    }

    private fun observeFlow() {
        lifecycleScope.launch {
            viewModel.searchPlaceData.collectLatest {
                Log.d(TAG, "observeFlow $it")
            }
        }
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

    private val getSearchResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK) {
                println("ok")
            }else {
                println("?")
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}