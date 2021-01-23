package ru.customelectronics.adsscreen.retrofit

import android.util.Log
import okhttp3.*
import java.net.SocketTimeoutException


class MyInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer ${RetrofitInstance.jwt}")
                .build()
        return chain.proceed(request)
    }
}