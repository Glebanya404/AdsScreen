package ru.customelectronics.adsscreen.api

import retrofit2.Response
import retrofit2.http.GET
import ru.customelectronics.adsscreen.model.Video

interface SimpleApi {

    @GET("api/24")
    suspend fun getVideo(): Response<Video>
}