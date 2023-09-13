package com.vcudemo.ui.map

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.kakaomobility.knsdk.KNCarType
import com.kakaomobility.knsdk.KNLanguageType
import com.kakaomobility.knsdk.KNRouteAvoidOption
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNError_Code_C302
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_CitsGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_GuideStateDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_RouteGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_SafetyGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_VoiceGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.component.MapViewCameraMode
import com.kakaomobility.location.library.BuildConfig
import com.vcudemo.R
import com.vcudemo.base.BaseActivity
import com.vcudemo.databinding.ActivityNavigationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NavigationActivity :
    BaseActivity(), KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate, KNGuidance_SafetyGuideDelegate,
    KNGuidance_RouteGuideDelegate, KNGuidance_VoiceGuideDelegate, KNGuidance_CitsGuideDelegate {
    companion object {
        const val TAG = "NavigationActivity"
    }

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var viewModel: NavigationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var startLatitude = 0.0
    private var startLongitude = 0.0
    private var destinationLatitude = 0.0
    private var destinationLongitude = 0.0
    private var rgCode = ""

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KNSDK.apply {
            // 콘텍스트 등록 및 DB, 파일 등의 저장 경로 설정
            install(application, "$filesDir/files")
            initializeWithAppKey(
                com.vcudemo.BuildConfig.KAKAO_NATIVE_APP_KEY,
                BuildConfig.VERSION_NAME,
                com.vcudemo.BuildConfig.USER_KEY,
                KNLanguageType.KNLanguageType_KOREAN,
                aCompletion = {
                    binding = DataBindingUtil.setContentView(this@NavigationActivity, R.layout.activity_navigation)
                    viewModel = ViewModelProvider(this@NavigationActivity)[NavigationViewModel::class.java]
                    observeFlow()

                    if (it != null) {
                        when (it.code) {
                            KNError_Code_C302 -> {
                                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                            }
                            else -> {
                                // todo : 추가 에러 작성
                                Log.d(TAG, "내비 초기화 실패")
                            }
                        }
                    } else {
                        Log.d(TAG, "내비 초기화 성공")
                        startLatitude = intent.getDoubleExtra("startLatitude", 0.0)
                        startLongitude = intent.getDoubleExtra("startLongitude", 0.0)
                        destinationLatitude = intent.getDoubleExtra("destinationLatitude", 0.0)
                        destinationLongitude = intent.getDoubleExtra("destinationLongitude", 0.0)

                        settingMap()
                        viewModel.getCoordConvertData(
                            startLatitude, startLongitude,
                            destinationLatitude, destinationLongitude
                        )
                    }
                })
        }
    }

    private fun observeFlow() {
        lifecycleScope.launch {
            viewModel.coordZipResult.collectLatest {
                Log.d(TAG, "좌표 변환 결과: $it")
                if(it.success == null) return@collectLatest

                val katechStartX = it.success.startLongitude!!.split(".")[0].toInt()
                val katechStartY = it.success.startLatitude!!.split(".")[0].toInt()
                val katechDestinationX = it.success.destinationLongitude!!.split(".")[0].toInt()
                val katechDestinationY = it.success.destinationLatitude!!.split(".")[0].toInt()

                // 출발지 설정
                val start = KNPOI("", katechStartX, katechStartY, null)

                // 목적지 설정
                val destination = KNPOI("", katechDestinationX, katechDestinationY, null)

                // 경로 생성
                KNSDK.makeTripWithStart(start, destination, null, null, aCompletion = { knError: KNError?, knTrip: KNTrip? ->
                    if (knError != null) {
                        Log.d(TAG, "경로 생성 에러(KNError: $knError")
                    }

                    // 경로 옵션 설정
                    val curRoutePriority = KNRoutePriority.valueOf(KNRoutePriority.KNRoutePriority_Recommand.toString())   // 경로 안내에서 우선적 고려 항목
                    val curAvoidOptions = KNRouteAvoidOption.KNRouteAvoidOption_None.value

                    knTrip?.routeWithPriority(curRoutePriority, curAvoidOptions) { error, _ ->
                        // 경로 요청 실패
                        if (error != null) {
                            Log.d(TAG, "경로 요청 실패 : $error")
                        }
                        // 경로 요청 성공
                        else {
                            Log.d(TAG, "경로 요청 성공")
                            KNSDK.sharedGuidance()?.apply {
                                // 각 가이던스 델리게이트 등록
                                guideStateDelegate = this@NavigationActivity
                                locationGuideDelegate = this@NavigationActivity
                                routeGuideDelegate = this@NavigationActivity
                                safetyGuideDelegate = this@NavigationActivity
                                voiceGuideDelegate = this@NavigationActivity
                                citsGuideDelegate = this@NavigationActivity

                                binding.naviView.initWithGuidance(
                                    this,
                                    knTrip,
                                    curRoutePriority,
                                    curAvoidOptions
                                )
                            }
                        }
                    }
                })
            }
        }

        lifecycleScope.launch {
            viewModel.distanceData.collectLatest {
                // todo : 실패 case 작성
                Log.d(TAG, "SK 직선 거리 : ${it.success?.distance}")
                if(it.success == null) return@collectLatest
                binding.tvInform.text = "다음 경로: $rgCode ${it.success?.distance}m"
            }
        }
    }

    /**
     * 지도 설정
     */
    private fun settingMap() {
        binding.naviView.mapViewMode = MapViewCameraMode.Top // 2D 모드
        binding.naviView.carType = KNCarType.KNCarType_Bike // 자동차: KNCarType_1, 오토바이: KNCarType_Bike
    }

    /**
     * Navi 경로 설정
     */
    private fun setNaviRoute() {
        // 출발지 설정, 회사
        // 37.4669433,126.8867386
        // 541116, 301718

    }

    /**
     * 경로 안내 정보 업데이트 시 호출
     * `routeGuide`의 항목이 1개 이상 변경 시 전달됨.
     */
    override fun guidanceDidUpdateRouteGuide(aGuidance: KNGuidance, aRouteGuide: KNGuide_Route) {
        binding.naviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)

        if(aRouteGuide.curDirection?.location?.pos != null && aRouteGuide.nextDirection?.location?.pos != null) {
            rgCode = aRouteGuide.nextDirection?.rgCode.toString()
            // todo : 모든 rgCode를 적용해야하나?
            when(rgCode) {
                "KNRGCode_Straight" -> {
                    rgCode = "직진"
                }
                "KNRGCode_LeftTurn" -> {
                    rgCode = "좌회전"
                }
                "KNRGCode_RightTurn" -> {
                    rgCode = "우회전"
                }
                "KNRGCode_UTurn" -> {
                    rgCode = "유턴"
                }
            }


            /**
             * SK 직선 거리 API 호출
             */
            viewModel.getDistanceData(
                aRouteGuide.curDirection?.location?.pos!!,
                aRouteGuide.nextDirection?.location?.pos!!
            )
        }
    }

    override fun guidanceCheckingRouteChange(aGuidance: KNGuidance) {
        binding.naviView.guidanceCheckingRouteChange(aGuidance)
    }

    override fun guidanceDidUpdateRoutes(
        aGuidance: KNGuidance,
        aRoutes: List<KNRoute>,
        aMultiRouteInfo: KNMultiRouteInfo?
    ) {
        binding.naviView.guidanceDidUpdateRoutes(aGuidance, aRoutes, aMultiRouteInfo)
    }

    override fun guidanceGuideEnded(aGuidance: KNGuidance) {
        binding.naviView.guidanceGuideEnded(aGuidance)
    }

    override fun guidanceGuideStarted(aGuidance: KNGuidance) {
        binding.naviView.guidanceGuideStarted(aGuidance)
    }

    override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
        binding.naviView.guidanceOutOfRoute(aGuidance)
    }

    override fun guidanceRouteChanged(aGuidance: KNGuidance) {
        binding.naviView.guidanceRouteChanged(aGuidance)
    }

    override fun guidanceRouteUnchanged(aGuidance: KNGuidance) {
        binding.naviView.guidanceRouteUnchanged(aGuidance)
    }

    override fun guidanceRouteUnchangedWithError(aGuidnace: KNGuidance, aError: KNError) {
        binding.naviView.guidanceRouteUnchangedWithError(aGuidnace, aError)
    }

    override fun guidanceDidUpdateLocation(
        aGuidance: KNGuidance,
        aLocationGuide: KNGuide_Location
    ) {
        binding.naviView.guidanceDidUpdateLocation(aGuidance, aLocationGuide)
    }

    override fun guidanceDidUpdateAroundSafeties(
        aGuidance: KNGuidance,
        aSafeties: List<KNSafety>?
    ) {
        binding.naviView.guidanceDidUpdateAroundSafeties(aGuidance, aSafeties)
    }

    override fun guidanceDidUpdateSafetyGuide(
        aGuidance: KNGuidance,
        aSafetyGuide: KNGuide_Safety?
    ) {
        binding.naviView.guidanceDidUpdateSafetyGuide(aGuidance, aSafetyGuide)
    }



    override fun didFinishPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        binding.naviView.didFinishPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun shouldPlayVoiceGuide(
        aGuidance: KNGuidance,
        aVoiceGuide: KNGuide_Voice,
        aNewData: MutableList<ByteArray>
    ): Boolean {
        return binding.naviView.shouldPlayVoiceGuide(aGuidance, aVoiceGuide, aNewData)
    }

    override fun willPlayVoiceGuide(aGuidance: KNGuidance, aVoiceGuide: KNGuide_Voice) {
        binding.naviView.willPlayVoiceGuide(aGuidance, aVoiceGuide)
    }

    override fun didUpdateCitsGuide(aGuidance: KNGuidance, aCitsGuide: KNGuide_Cits) {
        binding.naviView.didUpdateCitsGuide(aGuidance, aCitsGuide)
    }
}