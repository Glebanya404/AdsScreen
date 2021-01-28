package ru.customelectronics.adsscreen.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitInstance {

    const val PRIMARY_URL = "http://192.168.0.105:8080/"
    var jwt:String = ""

    private val client = OkHttpClient.Builder().apply {
        addInterceptor(MyInterceptor())
        connectTimeout(15, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
    }.build()



    private var retrofit = Retrofit.Builder()
            .baseUrl(PRIMARY_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    var api: SimpleApi = retrofit.create(SimpleApi::class.java)

    fun setNewUrl(url: String) {
        if (retrofit.baseUrl().toString() == url) return
        retrofit = Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        api = retrofit.create(SimpleApi::class.java)
    }
}