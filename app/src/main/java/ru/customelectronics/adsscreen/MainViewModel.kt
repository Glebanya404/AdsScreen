package ru.customelectronics.adsscreen

import android.util.Log
import androidx.lifecycle.*
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.customelectronics.adsscreen.retrofit.RetrofitInstance
import ru.customelectronics.adsscreen.model.User
import ru.customelectronics.adsscreen.model.Video
import ru.customelectronics.adsscreen.repository.Repository
import java.io.*

class MainViewModel(private val repository: Repository): ViewModel() {

    enum class ConnectionState (val msg: String){
        CONNECTED("Connected"),
        NOT_CONNECTED("Not connected"),
        ERROR401("Incorrect password")
    }

    val connectionState: MutableLiveData<ConnectionState> = MutableLiveData()
    val videoList: MutableLiveData<List<Video>> = MutableLiveData()

    val TAG = javaClass.name



    fun checkServerUpdate() {
        getServerVideoList()
    }

    fun getJwt() {
        val signInCall = repository.signIn(User("foo", "foo"))
        signInCall.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    RetrofitInstance.jwt = JSONObject(response.body().toString()).getString("jwt")
                }
                connectionState.value = ConnectionState.CONNECTED
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
                connectionState.value = ConnectionState.NOT_CONNECTED
            }
        })
    }

    fun getServerVideoList() {
        val getVideosCall = repository.getVideos()
        getVideosCall.enqueue(object : Callback<List<Video>> {
            override fun onResponse(call: Call<List<Video>>, response: Response<List<Video>>) {
                if (response.isSuccessful) {
                    videoList.value = response.body()
                    connectionState.value = ConnectionState.CONNECTED
                } else if (response.code() == 401 && connectionState.value == ConnectionState.CONNECTED) {
                    connectionState.value = ConnectionState.ERROR401
                    getJwt()
                    getServerVideoList()
                } else if (connectionState.value == ConnectionState.ERROR401){

                }
            }
            override fun onFailure(call: Call<List<Video>>, t: Throwable) {
                connectionState.value = ConnectionState.NOT_CONNECTED
            }
        })
    }

    fun getVideo(number: Int) {

    }


    fun downloadVideo(id: Int, fileDir: String) {

    }

    private suspend fun writeResponseBodyToDisk(body: ResponseBody, fileDir: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val videoFile = File( "$fileDir${File.separator}video.mp4")

                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null

                try {
                    val fileReader = ByteArray(4096)
                    val fileSize = body.contentLength()
                    var fileSizeDownloaded: Long = 0

                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(videoFile)

                    while (true) {
                        val read = inputStream.read(fileReader)
                        if (read == -1)break
                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded+=read
                        //Log.d(TAG, "writeResponseBodyToDisk: File download: $fileSizeDownloaded of $fileSize")
                    }
                    outputStream.flush()
                    true
                } catch (e:  IOException) {
                    false
                } finally {
                    inputStream?.close()
                    outputStream?.close()
                }
            } catch (e: IOException){
                false
            }
        }
    }
}