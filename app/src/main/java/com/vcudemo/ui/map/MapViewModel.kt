package com.vcudemo.ui.map

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

class MapViewModel: ViewModel() {
    private val _initMyLocationData: MutableLiveData<Location> = MutableLiveData()
    val initMyLocationData = _initMyLocationData

    private val _updateMyLocationData: MutableLiveData<Location> = MutableLiveData()
    val updateMyLocationData = _updateMyLocationData

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun getInitMyLocationData(fusedLocationClient: FusedLocationProviderClient) {
        fusedLocationClient.getCurrentLocation(
            LocationRequest.QUALITY_HIGH_ACCURACY, createCancellationToken()
        ).addOnSuccessListener { location ->
            if(location == null) {
                _initMyLocationData.value = null
            } else {
                _initMyLocationData.value = location
            }

        }
    }

    @SuppressLint("MissingPermission")
    fun getUpdateMyLocationData(fusedLocationClient: FusedLocationProviderClient) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if(location == null) {
                    _updateMyLocationData.value = null
                } else {
                    _updateMyLocationData.value = location
                }
            }
    }

    private fun createCancellationToken() = object: CancellationToken() {
        override fun isCancellationRequested(): Boolean = false

        override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken =
            CancellationTokenSource().token

    }
}