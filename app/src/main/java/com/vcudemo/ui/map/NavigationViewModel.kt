package com.vcudemo.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakaomobility.knsdk.common.util.FloatPoint
import com.vcudemo.data.navigation.DistanceResult
import com.vcudemo.repository.NavigationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(private val navigationRepository: NavigationRepository) : ViewModel() {
//    private val _distanceData: MutableLiveData<DistanceResult> = MutableLiveData()
//    val distanceData: LiveData<DistanceResult> = _distanceData

    private val _distanceData: MutableStateFlow<DistanceResult> = MutableStateFlow(DistanceResult())
    val distanceData: StateFlow<DistanceResult> = _distanceData



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
}