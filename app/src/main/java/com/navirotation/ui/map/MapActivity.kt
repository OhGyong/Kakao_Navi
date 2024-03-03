package com.navirotation.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import com.navirotation.R
import com.navirotation.base.BaseActivity
import com.navirotation.databinding.ActivityMapBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapActivity: BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var viewModel: MapViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var naverMap: NaverMap

    private val permissionArray = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // 내 위치(출발지) 좌표
    private var myLatitude = 0.0
    private var myLongitude = 0.0

    // 도착지 좌표
    private var endLatitude = 0.0
    private var endLongitude = 0.0

    // 내 위치(출발지) 마커
    private val myLocationMarker = Marker()

    // 도착지 마커
    private val endMarker = Marker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(VCU_DEMO, "onCreate()")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(com.navirotation.BuildConfig.NAVER_CLIENT_ID)

        onClickListener()
        observeLiveData()

        if(permissionArray.all{
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                viewModel.getInitMyLocationData(fusedLocationClient)
            } else {
                myLatitude = 37.55453
                myLongitude = 126.97071
                binding.mapView.getMapAsync(this)
            }
        } else {
            requestPermissions(permissionArray, 1)
        }
    }

    private fun observeLiveData() {
        viewModel.initMyLocationData.observe(this) {
            Log.d(VCU_DEMO, "getInitMyLocationData(): $it")

            if (it != null) {
                myLatitude = it.latitude
                myLongitude = it.longitude

            } else {
                // 위치 정보를 얻지 못했을 경우 임시 좌표 값
                myLatitude = 37.55453
                myLongitude = 126.97071
            }

            binding.mapView.getMapAsync(this)

            setMyLocationMarker()
            setLocationCamera(myLatitude, myLongitude)
        }

        viewModel.updateMyLocationData.observe(this) {
            Log.d(VCU_DEMO, "updateMyLocationData(): $it")
            if (it != null) {
                myLatitude = it.latitude
                myLongitude = it.longitude

            } else {
                // 위치 정보를 얻지 못했을 경우 임시 좌표 값
                myLatitude = 37.55453
                myLongitude = 126.97071
            }

            setMyLocationMarker()
            setLocationCamera(myLatitude, myLongitude)
        }
    }

    /**
     * 권한 핸들링
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    viewModel.getInitMyLocationData(fusedLocationClient)
                } else {
                    myLatitude = 37.55453
                    myLongitude = 126.97071
                    binding.mapView.getMapAsync(this)
                }
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    requestPermissions(permissionArray, 1)
                }else{
                    Toast.makeText(this, "권한 다시 묻지 않음 상태입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(VCU_DEMO, "onMapReady()")

        this.naverMap = naverMap
    }

    private fun onClickListener() {
        binding.ibMyLocation.setOnClickListener {
            viewModel.getUpdateMyLocationData(fusedLocationClient)
        }

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
            intent.putExtra("endLatitude", endLatitude)
            intent.putExtra("endLongitude", endLongitude)
            startActivity(intent)
            finish()
        }
    }

    /**
     * 내 위치 마커 설정
     */
    private fun setMyLocationMarker() {
        myLocationMarker.map = null
        myLocationMarker.width = 70
        myLocationMarker.height = 100
        myLocationMarker.position = LatLng(myLatitude, myLongitude)
        myLocationMarker.map = naverMap
    }

    /**
     * 카메라 이동 설정
     */
    private fun setLocationCamera(latitude: Double, longitude: Double) {
        val cameraPosition = CameraPosition(LatLng(latitude, longitude), 13.0)
        naverMap.cameraPosition = cameraPosition
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
                endMarker.map = null
                endMarker.width = 70
                endMarker.height = 100
                endMarker.icon = MarkerIcons.BLACK
                endMarker.iconTintColor = Color.RED
                endMarker.position = LatLng(endLatitude, endLongitude)
                endMarker.map = naverMap

                setLocationCamera(endLatitude, endLongitude)
            }else {
                // todo : 그냥 돌아왔을 때 처리하게 있나?
            }
        }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}