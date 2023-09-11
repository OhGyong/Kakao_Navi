package com.vcudemo.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vcudemo.data.map.SearchPlaceResult
import com.vcudemo.repository.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val mapRepository: MapRepository): ViewModel() {
    private val _searchPlaceData: MutableStateFlow<SearchPlaceResult> = MutableStateFlow(SearchPlaceResult())
    val searchPlaceData: StateFlow<SearchPlaceResult> = _searchPlaceData

    fun getSearchPlaceData(x: String, y: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try{
                _searchPlaceData.value = SearchPlaceResult(
                    success = mapRepository.getSearchPlaceData(x, y)
                )
            } catch (e: Exception) {
                _searchPlaceData.value = SearchPlaceResult(failure = e)
            }
        }
    }

}