package com.vcudemo.data.navigation

data class DistanceResult(
    val success: DistanceResponse.DistanceInfo? = null,
    val failure: Exception? = null
)
