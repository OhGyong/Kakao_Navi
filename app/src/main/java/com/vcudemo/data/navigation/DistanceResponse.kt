package com.vcudemo.data.navigation

import com.google.gson.annotations.SerializedName

data class DistanceResponse(
    @SerializedName("distanceInfo") val distanceInfo: DistanceInfo
) {
    data class DistanceInfo(
        @SerializedName("distance") val distance: Int
    )
}