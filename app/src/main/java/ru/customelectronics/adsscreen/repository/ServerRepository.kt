package ru.customelectronics.adsscreen.repository

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import ru.customelectronics.adsscreen.model.Url
import ru.customelectronics.adsscreen.retrofit.RetrofitInstance
import ru.customelectronics.adsscreen.model.User
import ru.customelectronics.adsscreen.model.Video
import java.util.*

class ServerRepository(private val macAddr: String) {

    suspend fun getVideos(): Response<List<Video>> {
        return RetrofitInstance.api.getVideos(macAddr)
    }


    fun getJwt(user: User): Call<JsonObject> {
        return RetrofitInstance.api.getJwt(macAddr,user)
    }

    fun downloadVideo(id: Long): Call<ResponseBody> {
        return RetrofitInstance.api.downloadVideo(id)
    }

    suspend fun getVideoQueue(): Response<Queue<Video>> {
        return RetrofitInstance.api.getVideoQueue(macAddr)
    }

    suspend fun getUrlList(): Response<List<Url>> {
        return RetrofitInstance.api.getUrlList()
    }
}