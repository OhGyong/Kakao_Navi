package com.vcudemo.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.vcudemo.data.navigation.DistanceResult
import com.vcudemo.repository.NavigationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NavigationViewModel: ViewModel() {
    private val _distanceData: MutableLiveData<DistanceResult> = MutableLiveData()
    val distanceData: LiveData<DistanceResult> = _distanceData

    fun getDistanceData(curDirection: FloatPoint, nextDirection: FloatPoint) {
        viewModelScope.launch(Dispatchers.Default) {
            try{
                _distanceData.postValue(
                    DistanceResult(success = NavigationRepository().getDistanceData(curDirection, nextDirection)?.distanceInfo)
                )
            } catch (e: Exception) {
                _distanceData.postValue(
                    DistanceResult(failure = e)
                )
            }
        }
    }
}