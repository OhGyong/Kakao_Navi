package com.navirotation.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.objects.KNError_Code_C103
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
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

    // 위치 권한 Array
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
        Log.d(NAVI_ROTATION, "onCreate()")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_map)
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(com.navirotation.BuildConfig.NAVER_CLIENT_ID)

        onClickListener()
        observeLiveData()

        /**
         * 권한 체크
         * - 권한 허용 O : 현재 위치 좌표 값 업데이트
         * - 권한 허용 X : 권한 요청
         */
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
            Log.d(NAVI_ROTATION, "getInitMyLocationData(): $it")

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
            Log.d(NAVI_ROTATION, "updateMyLocationData(): $it")
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
        Log.d(NAVI_ROTATION, "onMapReady()")

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
            // 카카오 내비게이션 인스톨 및 초기화
            KNSDK.apply {
                install(application, "$filesDir/files") // 콘텍스트 등록 및 DB, 파일 등의 저장 경로 설정
                initializeWithAppKey(
                    com.navirotation.BuildConfig.KAKAO_NATIVE_APP_KEY,
                    com.navirotation.BuildConfig.VERSION_NAME,
                    null,
                    KNLanguageType.KNLanguageType_KOREAN,
                    aCompletion = {
                        if (it != null) {
                            when (it.code) {
                                KNError_Code_C103 -> {
                                    Log.d(NAVI_ROTATION, "내비 인증 실패: $it")
                                    return@initializeWithAppKey
                                }
                                KNError_Code_C302 -> {
                                    Log.d(NAVI_ROTATION, "내비 권한 오류 : $it")
                                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                                    return@initializeWithAppKey
                                }
                                else -> {
                                    Log.d(NAVI_ROTATION, "내비 초기화 실패: $it")
                                    return@initializeWithAppKey
                                }
                            }
                        } else {
                            Log.d(NAVI_ROTATION, "내비 초기화 성공")
                            Handler(Looper.getMainLooper()).post {
                                val intent = Intent(applicationContext, NavigationActivity::class.java)
                                intent.putExtra("startLatitude", myLatitude)
                                intent.putExtra("startLongitude", myLongitude)
                                intent.putExtra("endLatitude", endLatitude)
                                intent.putExtra("endLongitude", endLongitude)
                                startActivity(intent)
                                finish()
                            }
                        }
                    })
            }
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

                    // 도착지 좌표 업데이트
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