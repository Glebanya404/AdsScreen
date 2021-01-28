package ru.customelectronics.adsscreen.retrofit

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import ru.customelectronics.adsscreen.model.Url
import ru.customelectronics.adsscreen.model.User
import ru.customelectronics.adsscreen.model.Video
import java.util.*

interface SimpleApi {

    @GET("api/getFor")
    suspend fun getVideos(@Query("macAddr") macAddr: String): Response<List<Video>>


    @Streaming
    @GET("api/download/{id}")
    fun downloadVideo(
            @Path("id") id: Long
    ): Call<ResponseBody>


    @POST("api/authenticate")
    fun getJwt(
        @Query("macAddr") macAddr: String,
        @Body user: User
    ): Call<JsonObject>

    @GET("api/getQueue")
    suspend fun getVideoQueue( @Query("macAddr") macAddr: String): Response<Queue<Video>>

    @GET("api/getUrlList")
    suspend fun getUrlList(): Response<List<Url>>

}