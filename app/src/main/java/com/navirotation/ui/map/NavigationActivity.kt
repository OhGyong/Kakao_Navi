package com.navirotation.ui.map

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kakaomobility.knsdk.KNCarType
import com.kakaomobility.knsdk.KNRouteAvoidOption
import com.kakaomobility.knsdk.KNRoutePriority
import com.kakaomobility.knsdk.KNSDK
import com.kakaomobility.knsdk.common.objects.KNError
import com.kakaomobility.knsdk.common.objects.KNPOI
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_CitsGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_GuideStateDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_LocationGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_RouteGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_SafetyGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuidance_VoiceGuideDelegate
import com.kakaomobility.knsdk.guidance.knguidance.KNGuideRouteChangeReason
import com.kakaomobility.knsdk.guidance.knguidance.citsguide.KNGuide_Cits
import com.kakaomobility.knsdk.guidance.knguidance.common.KNLocation
import com.kakaomobility.knsdk.guidance.knguidance.locationguide.KNGuide_Location
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.KNGuide_Route
import com.kakaomobility.knsdk.guidance.knguidance.routeguide.objects.KNMultiRouteInfo
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.KNGuide_Safety
import com.kakaomobility.knsdk.guidance.knguidance.safetyguide.objects.KNSafety
import com.kakaomobility.knsdk.guidance.knguidance.voiceguide.KNGuide_Voice
import com.kakaomobility.knsdk.trip.kntrip.KNTrip
import com.kakaomobility.knsdk.trip.kntrip.knroute.KNRoute
import com.kakaomobility.knsdk.ui.component.MapViewCameraMode
import com.navirotation.R
import com.navirotation.base.BaseActivity
import com.navirotation.databinding.ActivityNavigationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NavigationActivity :
    BaseActivity(), KNGuidance_GuideStateDelegate,
    KNGuidance_LocationGuideDelegate, KNGuidance_SafetyGuideDelegate,
    KNGuidance_RouteGuideDelegate, KNGuidance_VoiceGuideDelegate, KNGuidance_CitsGuideDelegate {
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var viewModel: NavigationViewModel

    // 출발지 좌표
    private var startLatitude = 0.0
    private var startLongitude = 0.0

    // 도착지 좌표
    private var endLatitude = 0.0
    private var endLongitude = 0.0

    // 회전 정보
    private var rgCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this@NavigationActivity, R.layout.activity_navigation)
        viewModel = ViewModelProvider(this@NavigationActivity)[NavigationViewModel::class.java]
        observeFlow()
        settingMap()

        startLatitude = intent.getDoubleExtra("startLatitude", 0.0)
        startLongitude = intent.getDoubleExtra("startLongitude", 0.0)
        endLatitude = intent.getDoubleExtra("endLatitude", 0.0)
        endLongitude = intent.getDoubleExtra("endLongitude", 0.0)

        viewModel.getCoordConvertData(
            startLatitude, startLongitude,
            endLatitude, endLongitude
        )
    }

    private fun observeFlow() {
        lifecycleScope.launch {
            viewModel.coordZipResult.collectLatest {
                Log.d(NAVI_ROTATION, "좌표 변환 결과: $it")
                if (it.success == null) return@collectLatest

                val katechStartX = it.success.startLongitude!!.split(".")[0].toInt()
                val katechStartY = it.success.startLatitude!!.split(".")[0].toInt()
                val katechEndX = it.success.endLongitude!!.split(".")[0].toInt()
                val katechEndY = it.success.endLatitude!!.split(".")[0].toInt()

                // 출발지 설정
                val start = KNPOI("", katechStartX, katechStartY, null)

                // 목적지 설정
                val end = KNPOI("", katechEndX, katechEndY, null)

                // 경로 생성
                KNSDK.makeTripWithStart(
                    start,
                    end,
                    null,
                    null,
                    aCompletion = { knError: KNError?, knTrip: KNTrip? ->
                        if (knError != null) {
                            Log.d(NAVI_ROTATION, "경로 생성 에러(KNError: $knError")
                        }

                        // 경로 옵션 설정
                        val curRoutePriority =
                            KNRoutePriority.valueOf(KNRoutePriority.KNRoutePriority_Recommand.toString())   // 경로 안내에서 우선적 고려 항목
                        val curAvoidOptions = KNRouteAvoidOption.KNRouteAvoidOption_None.value

                        knTrip?.routeWithPriority(curRoutePriority, curAvoidOptions) { error, _ ->
                            // 경로 요청 실패
                            if (error != null) {
                                Log.d(NAVI_ROTATION, "경로 요청 실패 : $error")
                            }
                            // 경로 요청 성공
                            else {
                                Log.d(NAVI_ROTATION, "경로 요청 성공")
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
                Log.d(NAVI_ROTATION, "SK 직선 거리 : ${it.success?.distance}")
                if (it.success == null) return@collectLatest
                binding.tvInform.text = "다음 경로: $rgCode ${it.success?.distance}m"
            }
        }
    }

    /**
     * 지도 설정
     */
    private fun settingMap() {
        binding.naviView.mapViewMode = MapViewCameraMode.Top // 2D 모드
        binding.naviView.carType =
            KNCarType.KNCarType_Bike // 자동차: KNCarType_1, 오토바이: KNCarType_Bike
    }

    /**
     * 뒤로 가기 이벤트
     * - Navigation 종료
     */
    override fun onBackPressed() {
        guidanceGuideEnded(binding.naviView.guidance)
    }

    /**
     * 경로 안내 정보 업데이트 시 호출
     * `routeGuide`의 항목이 1개 이상 변경 시 전달됨.
     */
    override fun guidanceDidUpdateRouteGuide(aGuidance: KNGuidance, aRouteGuide: KNGuide_Route) {
        binding.naviView.guidanceDidUpdateRouteGuide(aGuidance, aRouteGuide)

        if (aRouteGuide.curDirection?.location?.pos != null && aRouteGuide.nextDirection?.location?.pos != null) {
            rgCode = aRouteGuide.nextDirection?.rgCode.toString()
            // todo : 모든 rgCode를 적용해야하나?
            when (rgCode) {
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
                aRouteGuide.curDirection?.location?.pos!!.toFloatPoint(),
                aRouteGuide.nextDirection?.location?.pos!!.toFloatPoint()
            )
        }
    }

    /**
     * 길 안내 종료 시 호출
     */
    override fun guidanceGuideEnded(aGuidance: KNGuidance) {
        Log.d(NAVI_ROTATION, "guidanceGuideEnded 내비게이션 종료")
        binding.naviView.guidanceGuideEnded(aGuidance, false) // 종료 팝업 노출x
        finish()
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

    override fun guidanceGuideStarted(aGuidance: KNGuidance) {
        binding.naviView.guidanceGuideStarted(aGuidance)
    }

    override fun guidanceOutOfRoute(aGuidance: KNGuidance) {
        binding.naviView.guidanceOutOfRoute(aGuidance)
    }

    override fun guidanceRouteChanged(
        aGuidance: KNGuidance,
        aFromRoute: KNRoute,
        aFromLocation: KNLocation,
        aToRoute: KNRoute,
        aToLocation: KNLocation,
        aChangeReason: KNGuideRouteChangeReason
    ) {
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