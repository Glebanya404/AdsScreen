package ru.customelectronics.adsscreen

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.customelectronics.adsscreen.model.Video
import ru.customelectronics.adsscreen.repository.Repository

class MainViewModel(private val repository: Repository): ViewModel() {

    val myResponse: MutableLiveData<Response<Video>> = MutableLiveData()

    fun getPost() {
        viewModelScope.launch {
            val response = repository.getVideo()
            myResponse.value = response
        }
    }
}