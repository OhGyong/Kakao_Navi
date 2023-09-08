package com.vcudemo.ui.map

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
class NavigationActivity : BaseActivity(), KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate, KNGuidance_SafetyGuideDelegate,
    KNGuidance_RouteGuideDelegate, KNGuidance_VoiceGuideDelegate, KNGuidance_CitsGuideDelegate {
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var viewModel: NavigationViewModel

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        KNSDK.apply {
            // 콘텍스트 등록 및 DB, 파일 등의 저장 경로 설정
            install(application, "$filesDir/files")
            initializeWithAppKey(
                com.vcudemo.BuildConfig.KAKAO_NAVIGATION_KEY,
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
                                println("내비 초기화 에러")
                            }
                        }
                    } else {
                        // todo : tag 달기
                        settingMap()
                        setNaviRoute()
                    }
                })
        }
    }

    private fun observeFlow() {
        lifecycleScope.launch {
            viewModel.distanceData.collectLatest {
                println(it)
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
        val start = KNPOI("독산역 1호선", 301718, 541116, null)

        // 목적지 설정, 가산디지털단지역
        // 37.4822545, 126.8821832
        // 542820, 301335
        val goal = KNPOI("가산디지털단지역 1호선", 301335, 542820, null)

        // 경로 생성
        KNSDK.makeTripWithStart(start, goal, null, null, aCompletion = { knError: KNError?, knTrip: KNTrip? ->
            if (knError != null) {
                println("KNError $knError")
            }

            // 경로 옵션 설정
            val curRoutePriority = KNRoutePriority.valueOf(KNRoutePriority.KNRoutePriority_Recommand.toString())   // 경로 안내에서 우선적 고려 항목
            val curAvoidOptions = KNRouteAvoidOption.KNRouteAvoidOption_None.value

            knTrip?.routeWithPriority(curRoutePriority, curAvoidOptions) { error, _ ->
                if (error != null) {
                    println("경로 요청 실패 $error")
                    // 경로 요청 실패
                } else {
                    // 경로 요청 성공
                    println("경로 요청 성공")
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

    // 경로 안내 정보 업데이트 시 호출. `routeGuide`의 항목이 1개 이상 변경 시 전달됨.
    override fun guidanceDidUpdateRouteGuide(aGuidance: KNGuidance, aRouteGuide: KNGuide_Route) {
        binding.naviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)

        if(aRouteGuide.curDirection?.location?.pos != null && aRouteGuide.nextDirection?.location?.pos != null) {
            // todo : 거리 계산 api 호출 및 ble 반환
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