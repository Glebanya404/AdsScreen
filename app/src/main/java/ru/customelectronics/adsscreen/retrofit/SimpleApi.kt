package ru.customelectronics.adsscreen.retrofit

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import ru.customelectronics.adsscreen.model.User
import ru.customelectronics.adsscreen.model.Video

interface SimpleApi {

    @GET("api")
    suspend fun getVideos(): Response<List<Video>>


    @Streaming
    @GET("api/download/{id}")
    fun downloadVideo(
            @Path("id") id: Long
    ): Call<ResponseBody>


    @POST("api/authenticate")
    suspend fun getJwt(
        @Body user: User
    ): Response<JsonObject>

}