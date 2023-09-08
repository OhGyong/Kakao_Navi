package com.vcudemo.repository

import com.kakaomobility.knsdk.common.util.FloatPoint
import com.vcudemo.data.navigation.DistanceResponse
import com.vcudemo.service.NavigationService
import javax.inject.Inject

class NavigationRepository @Inject constructor(private val navigationService: NavigationService) {
    fun getDistanceData(curDirection: FloatPoint, nextDirection: FloatPoint) : DistanceResponse? {
        return navigationService.distanceRequest(
            appKey = com.vcudemo.BuildConfig.SK_APP_KEY,
            version = "1",
            startX = curDirection.x.toString(),
            startY = curDirection.y.toString(),
            endX = nextDirection.x.toString(),
            endY = nextDirection.y.toString(),
            "KATECH"
        ).execute().body()
    }
}