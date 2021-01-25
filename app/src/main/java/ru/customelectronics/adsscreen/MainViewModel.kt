package ru.customelectronics.adsscreen

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.log

class MainViewModel(private val serverRepository: ServerRepository, val sqlRepository: SqlRepository, val filesDir: String): ViewModel() {

    enum class ConnectionState (val msg: String){
        READY("Ready"),
        WORKING("Work in progress"),
        NOT_CONNECTED("Not connected"),
        ERROR401("Incorrect password"),
        ERROR("Error")
    }

    private val serverController = ServerController()
    val sqlController = SqlController()

    val connectionState: MutableLiveData<ConnectionState> = MutableLiveData()
    val serverVideoList: MutableLiveData<List<Video>> = MutableLiveData()
    var defaultQueue: MutableLiveData<Queue<Video>> = MutableLiveData()
    var currentQueue: Queue<Video> = LinkedList(listOf())
    var sqlVideoList = sqlRepository.getAll

    val TAG = javaClass.name



    fun checkServerUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            //If not connected trya to connect
            if (connectionState.value != ConnectionState.READY) {
                try {
                    serverController.getJwt()
                } catch (e: Exception) {
                    connectionState.postValue(ConnectionState.ERROR)
//                    throw e
                    e.printStackTrace()
                    return@launch
                }
            }
            try {
                Log.d(TAG, "checkServerUpdate: getServerVideoList")
                serverVideoList.postValue(serverController.getServerVideoList())//Get current video list
                Log.d(TAG, "checkServerUpdate: findNotDownloadedVideo")
                val toDownloadVideoList = findNotDownloadedVideo()// Find new videos
                Log.d(TAG, "checkServerUpdate: Download videos")
                Log.d(TAG, "checkServerUpdate: $toDownloadVideoList")
                for (video in toDownloadVideoList) {
                    val async = async {
                        Log.d(TAG, "checkServerUpdate: Start download ${video.title}")
                        serverController.downloadVideo(video)//Download each new video
                    }
                    Log.d(TAG, "checkServerUpdate: Waiting")
                    async.await()
                }
                defaultQueue.postValue(serverController.getVideoQueue())

                connectionState.postValue(ConnectionState.READY)
            } catch (e: Exception) {
                connectionState.postValue(ConnectionState.ERROR)
//                throw e
                e.printStackTrace()
            }
        }
    }


    private fun findNotDownloadedVideo(): List<Video> {
        if (serverVideoList.value == null || serverVideoList.value!!.isEmpty()) return emptyList()
        if (sqlVideoList.value == null || sqlVideoList.value!!.isEmpty()) return serverVideoList.value!!
        return serverVideoList.value!!.filter { !sqlVideoList.value!!.contains(it)}
    }

    fun getNextVideo(): String {
        if (currentQueue.size == 0){
            currentQueue = LinkedList(defaultQueue.value ?: listOf())
        }
        val video = currentQueue.poll()
        val file = video?.fileName ?: ""
        return "$filesDir$file"
    }


    inner class ServerController() {
        suspend fun getJwt() {
            connectionState.postValue(ConnectionState.WORKING)
            val response = serverRepository.getJwt(User("foo", "foo"))
            if (response.isSuccessful) {
                RetrofitInstance.jwt = JSONObject(response.body().toString()).getString("jwt")
            } else {
                connectionState.postValue(ConnectionState.ERROR)
            }
        }

        suspend fun getServerVideoList():  List<Video>{
            connectionState.postValue(ConnectionState.WORKING)
            val response = serverRepository.getVideos()
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            } else if (response.code() == 401 && connectionState.value != ConnectionState.ERROR401) {
                connectionState.postValue(ConnectionState.ERROR401)
                getJwt()
                return getServerVideoList()
            }
            return emptyList()
        }

        fun downloadVideo(video: Video) {
            connectionState.postValue(ConnectionState.WORKING)
            val response = serverRepository.downloadVideo(video.id).execute()
            if (response.isSuccessful){
                val success = writeResponseBodyToDisk(response.body()!!, filesDir, video.fileName)
                if (success) SqlController().addVideo(video)
            }
        }

        private fun writeResponseBodyToDisk(
            body: ResponseBody,
            filesDir: String,
            fileName: String
        ): Boolean {
            return try {
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

        suspend fun getVideoQueue(): Queue<Video>{
            connectionState.postValue(ConnectionState.WORKING)
            val response = serverRepository.getVideoQueue()
            if (response.isSuccessful){
                return (response.body() ?: emptyList<Video>()) as Queue<Video>
            } else if (response.code() == 401 && connectionState.value != ConnectionState.ERROR401) {
                connectionState.postValue(ConnectionState.ERROR401)
                getJwt()
                return getVideoQueue()
            }
            return LinkedList(listOf())
        }

    }

    inner class SqlController {
        fun addVideo(video: Video) {
            viewModelScope.launch {
                sqlRepository.addVideo(video)
            }
        }
    }


}

