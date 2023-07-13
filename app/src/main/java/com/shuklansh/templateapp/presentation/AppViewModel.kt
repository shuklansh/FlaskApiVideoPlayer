package com.shuklansh.templateapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shuklansh.templateapp.data.remote.flaskVideoApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    val api : flaskVideoApi
) : ViewModel() {

    private var _listOfVideos = MutableStateFlow(ResponseState().listofvideosfromapi)
    val listOfVideos = _listOfVideos.asStateFlow()

    suspend fun updateVideoList(){
        viewModelScope.launch {
            _listOfVideos.update {
                api.getApiData()
            }
        }
    }

}