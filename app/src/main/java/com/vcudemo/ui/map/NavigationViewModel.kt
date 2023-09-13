package com.vcudemo.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.vcudemo.data.navigation.CoordZipData
import com.vcudemo.data.navigation.CoordZipResult
import com.vcudemo.data.navigation.DistanceResult
import com.vcudemo.repository.NavigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(private val navigationRepository: NavigationRepository) : ViewModel() {
    private val _distanceData: MutableStateFlow<DistanceResult> = MutableStateFlow(DistanceResult())
    val distanceData: StateFlow<DistanceResult> = _distanceData

    private val _coordZipResult: MutableStateFlow<CoordZipResult> = MutableStateFlow(CoordZipResult())
    val coordZipResult: StateFlow<CoordZipResult> = _coordZipResult



    fun getDistanceData(curDirection: FloatPoint, nextDirection: FloatPoint) {
        viewModelScope.launch(Dispatchers.Default) {
            try{
                _distanceData.value = DistanceResult(
                    success = navigationRepository
                        .getDistanceData(curDirection, nextDirection)
                        ?.distanceInfo
                )
            } catch (e: Exception) {
                _distanceData.value = DistanceResult(failure = e)
            }
        }
    }

    fun getCoordConvertData(
        startLatitude: Double, startLongitude: Double,
        destinationLatitude: Double, destinationLongitude: Double
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val startLocationFlow = flow {
                    emit(navigationRepository.getCoordConvertData(startLatitude, startLongitude)?.coordinate)
                }

                val destinationLocationFlow = flow {
                    emit(navigationRepository.getCoordConvertData(destinationLatitude, destinationLongitude)?.coordinate)
                }

                startLocationFlow.zip(destinationLocationFlow) { startLocation, destinationResult ->
                    CoordZipResult(
                        success = CoordZipData(
                            startLocation?.lat,
                            startLocation?.lon,
                            destinationResult?.lat,
                            destinationResult?.lon
                        )
                    )
                }.collect { coordZipResult->
                    _coordZipResult.value = coordZipResult
                }
            }catch (e: Exception) {
                _coordZipResult.value = CoordZipResult(failure = e)
            }
        }
    }
}