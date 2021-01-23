package ru.customelectronics.adsscreen.repository

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import ru.customelectronics.adsscreen.retrofit.RetrofitInstance
import ru.customelectronics.adsscreen.model.User
import ru.customelectronics.adsscreen.model.Video

class ServerRepository {

    suspend fun getVideos(): Response<List<Video>> {
        return RetrofitInstance.api.getVideos()
    }


    suspend fun getJwt(user: User): Response<JsonObject> {
        return RetrofitInstance.api.getJwt(user)
    }

    fun downloadVideo(id: Long): Call<ResponseBody> {
        return RetrofitInstance.api.downloadVideo(id)
    }
}