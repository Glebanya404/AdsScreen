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
    fun getVideos(): Call<List<Video>>

    @GET("api/{videoNumber}")
    fun getVideo(
        @Path("videoNumber") number: Int
    ): Call<Video>

    @Streaming
    @GET("api/download/{id}")
    fun downloadVideo(
            @Path("id") id: Int
    ): Call<ResponseBody>


    @POST("api/authenticate")
    fun signIn(
        @Body user: User
    ): Call<JsonObject>

}