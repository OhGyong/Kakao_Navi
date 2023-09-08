package com.vcudemo.data.navigation

data class CoordConvertResult (
    val success: CoordConvertResponse.LocationInfo? = null,
    val failure: Exception? = null
)