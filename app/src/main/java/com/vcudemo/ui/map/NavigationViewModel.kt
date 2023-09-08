package com.vcudemo.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.vcudemo.data.navigation.CoordConvertResult
import com.vcudemo.data.navigation.DistanceResult
import com.vcudemo.repository.NavigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(private val navigationRepository: NavigationRepository) : ViewModel() {
    private val _distanceData: MutableStateFlow<DistanceResult> = MutableStateFlow(DistanceResult())
    val distanceData: StateFlow<DistanceResult> = _distanceData

    private val _coordConvertData: MutableStateFlow<CoordConvertResult> = MutableStateFlow(CoordConvertResult())
    val coordConvertData: StateFlow<CoordConvertResult> = _coordConvertData



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

    fun getCoordConvertData(myLatitude: Double, myLongitude: Double) {
        println("$myLatitude   $myLongitude ")
        viewModelScope.launch(Dispatchers.Default) {
            try{
                _coordConvertData.value = CoordConvertResult(
                    success = navigationRepository
                        .getCoordConvertData(myLatitude, myLongitude)
                        ?.coordinate
                )
            } catch (e: Exception) {
                _coordConvertData.value = CoordConvertResult(failure = e)
            }
        }
    }
}