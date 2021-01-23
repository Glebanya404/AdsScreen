package ru.customelectronics.adsscreen

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Callback
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import ru.customelectronics.adsscreen.retrofit.RetrofitInstance
import ru.customelectronics.adsscreen.model.User
import ru.customelectronics.adsscreen.model.Video
import ru.customelectronics.adsscreen.repository.ServerRepository
import ru.customelectronics.adsscreen.repository.SqlRepository
import java.io.*
import java.lang.Exception
import kotlin.concurrent.thread

class MainViewModel(private val serverRepository: ServerRepository, val sqlRepository: SqlRepository, val filesDir: String): ViewModel() {

    enum class ConnectionState (val msg: String){
        READY("Ready"),
        WORKING("Work in progress"),
        NOT_CONNECTED("Not connected"),
        ERROR401("Incorrect password"),
        ERROR("Error")
    }

    val serverController = ServerController()
    val sqlController = SqlController()

    val connectionState: MutableLiveData<ConnectionState> = MutableLiveData()
    val serverVideoList: MutableLiveData<List<Video>> = MutableLiveData()
    val sqlVideoList = sqlRepository.getAll

    val TAG = javaClass.name



    fun checkServerUpdate() {
        viewModelScope.launch {
            if (connectionState.value == ConnectionState.NOT_CONNECTED) {
                try {
                    serverController.getJwt()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@launch
                }
            }

            try {
                connectionState.value = ConnectionState.WORKING

                serverController.getServerVideoList()//Get current video list
                val toDownloadVideoList = findNotDownloadedVideo()// Find new videos
                for (video in toDownloadVideoList) {
                        serverController.downloadVideo(video)//Download each new video
                }

                connectionState.value = ConnectionState.READY
            } catch (e: Exception) {
                e.printStackTrace()
                connectionState.value = ConnectionState.NOT_CONNECTED
            }
        }
    }


    private fun findNotDownloadedVideo(): List<Video> {
        if (serverVideoList.value == null || serverVideoList.value!!.isEmpty()) return emptyList()
        if (sqlVideoList.value == null || sqlVideoList.value!!.isEmpty()) return serverVideoList.value!!
        return serverVideoList.value!!.filter { !sqlVideoList.value!!.contains(it)}
    }



    inner class ServerController() {
        suspend fun getJwt() {
            val response = serverRepository.getJwt(User("foo", "foo"))
            if (response.isSuccessful) {
                RetrofitInstance.jwt = JSONObject(response.body().toString()).getString("jwt")
            } else {
                connectionState.value = ConnectionState.ERROR
            }
        }

        suspend fun getServerVideoList() {
            val response = serverRepository.getVideos()
            if (response.isSuccessful) {
                serverVideoList.value = response.body()
            } else if (response.code() == 401 && connectionState.value != ConnectionState.ERROR401) {
                connectionState.value = ConnectionState.ERROR401
                getJwt()
                getServerVideoList()
            }
        }

        fun downloadVideo(video: Video) {
            val response = serverRepository.downloadVideo(video.id)
            response.enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful){
                        thread {
                            val success = writeResponseBodyToDisk(response.body()!!, filesDir, video.fileName)
                            Log.d(TAG, "downloadVideo: Video${video.id} download->$success")
                            video.isDownloaded = true
                            if (success) SqlController().addVideo(video)
                        }
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    throw t
                }

            })


        }

        private fun writeResponseBodyToDisk(
            body: ResponseBody,
            filesDir: String,
            fileName: String
        ): Boolean {
            return try {
                Log.d(TAG, "writeResponseBodyToDisk: Start")
                val videoFile = File( "$filesDir${File.separator}$fileName")

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
                        Log.d(TAG, "writeResponseBodyToDisk: File download: $fileSizeDownloaded of $fileSize")
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

    inner class SqlController() {
        fun addVideo(video: Video) {
            viewModelScope.launch {
                sqlRepository.addVideo(video)
            }
        }
    }


}

