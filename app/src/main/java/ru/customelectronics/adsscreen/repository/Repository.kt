package ru.customelectronics.adsscreen.repository

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import ru.customelectronics.adsscreen.retrofit.RetrofitInstance
import ru.customelectronics.adsscreen.model.User
import ru.customelectronics.adsscreen.model.Video

class Repository {

    fun getVideos(): Call<List<Video>> {
        return RetrofitInstance.api.getVideos()
    }

    fun getVideo(number: Int): Call<Video> {
        return RetrofitInstance.api.getVideo(number)
    }

    fun signIn(user: User): Call<JsonObject> {
        return RetrofitInstance.api.signIn(user)
    }

    fun downloadVideo(id: Int): Call<ResponseBody> {
        return RetrofitInstance.api.downloadVideo(id)
    }
}