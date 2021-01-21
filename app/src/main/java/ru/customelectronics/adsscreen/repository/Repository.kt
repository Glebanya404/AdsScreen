package ru.customelectronics.adsscreen.repository

import retrofit2.Response
import ru.customelectronics.adsscreen.api.RetrofitInstance
import ru.customelectronics.adsscreen.model.Video

class Repository {

    suspend fun getVideo(): Response<Video> {
        return RetrofitInstance.api.getVideo()
    }
}