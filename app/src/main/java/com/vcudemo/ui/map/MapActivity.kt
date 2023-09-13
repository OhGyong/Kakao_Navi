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
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var binding: ActivityMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private var myLatitude = 0.0
    private var myLongitude = 0.0
    private var destinationLatitude = 0.0
    private var destinationLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
//        locationSource =
//            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(com.vcudemo.BuildConfig.NAVER_CLIENT_ID)

        getMyLocation()

        binding.etSearch.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("latitude", myLatitude.toString())
            intent.putExtra("longitude", myLongitude.toString())
            getSearchResult.launch(intent)
        }

        binding.ctDetail.setOnClickListener {
        }

        binding.btnIntentNavi.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            intent.putExtra("startLatitude", myLatitude)
            intent.putExtra("startLongitude", myLongitude)
            intent.putExtra("destinationLatitude", myLatitude)
            intent.putExtra("destinationLongitude", destinationLongitude)
            startActivity(intent)
            finish()
        }

    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady()")

        this.naverMap = naverMap

        // 위치 추적 모드 on
//        naverMap.locationSource = locationSource
//        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow

        // 현재 위치 버튼 on
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 카메라 줌 레벨 및 카메라 이동
        val cameraPosition = CameraPosition(LatLng(myLatitude, myLongitude), 13.0)
        naverMap.cameraPosition = cameraPosition
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
            if(result.resultCode == RESULT_OK) {
                if(result.data != null) {
                    binding.etSearch.setText(result.data!!.getStringExtra("placeName"))
                    binding.tvPlaceName.text = result.data!!.getStringExtra("placeName")
                    binding.tvAddressName.text = result.data!!.getStringExtra("addressName")
                    binding.tvDistance.text = result.data!!.getStringExtra("distance")
                    destinationLatitude = result.data!!.getDoubleExtra("latitude", 0.0)
                    destinationLongitude = result.data!!.getDoubleExtra("longitude", 0.0)

                    binding.ctDetail.visibility = View.VISIBLE
                }
                val marker = Marker()
                marker.width = 70
                marker.height = 100
                marker.position = LatLng(destinationLatitude, destinationLongitude)
                marker.map = naverMap

                // 카메라 줌 레벨 및 카메라 이동
                val cameraPosition = CameraPosition(LatLng(destinationLatitude, destinationLongitude), 13.0)
                naverMap.cameraPosition = cameraPosition

            }else {
                // todo : 그냥 돌아왔을 때 처리하게 있나?
            }
        }

//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>,
//                                            grantResults: IntArray) {
//        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
//                grantResults)) {
//            if (!locationSource.isActivated) { // 권한 거부됨
//            }
//            return
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
}