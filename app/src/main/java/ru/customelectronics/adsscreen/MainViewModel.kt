package ru.customelectronics.adsscreen

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import ru.customelectronics.adsscreen.model.Url
import ru.customelectronics.adsscreen.retrofit.RetrofitInstance
import ru.customelectronics.adsscreen.model.User
import ru.customelectronics.adsscreen.model.Video
import ru.customelectronics.adsscreen.repository.ServerRepository
import ru.customelectronics.adsscreen.repository.SqlRepository
import java.io.*
import java.lang.Exception
import java.util.*

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
    var sqlVideoList = sqlRepository.getAllVideos
    var sqlUrlList = sqlRepository.getAllUrls

    val TAG = javaClass.name



    fun checkServerUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            if (connectionState.value == ConnectionState.WORKING) return@launch

            //If not connected trya to connect
            if (connectionState.value != ConnectionState.READY) {
                if (!serverController.connect()) return@launch
            }

            try {
                serverVideoList.postValue(serverController.getServerVideoList())//Get current video list

                val toDeleteVideoList = findVideosToDelete()//Delete old videos
                for (video in toDeleteVideoList) {
                    sqlController.deleteVideo(video)
                    File("$filesDir${video.fileName}").delete()
                }

                val toDownloadVideoList = findNotDownloadedVideos()// Find new videos
                for (video in toDownloadVideoList) {
                    val async = async {
                        Log.d(TAG, "checkServerUpdate: Start download ${video.title}")
                        serverController.downloadVideo(video)//Download each new video
                    }
                    async.await()
                }

                val serverUrlList = serverController.getUrlList()
                sqlController.replaceUrlList(serverUrlList)

                defaultQueue.postValue(serverController.getVideoQueue())
                connectionState.postValue(ConnectionState.READY)
            } catch (e: Exception) {
                connectionState.postValue(ConnectionState.ERROR)
//                throw e
                e.printStackTrace()
            }
        }
    }



    private fun findVideosToDelete(): List<Video> {
        if (sqlVideoList.value == null || sqlVideoList.value!!.isEmpty()) return emptyList()
        if (serverVideoList.value == null || serverVideoList.value!!.isEmpty()) return sqlVideoList.value ?: emptyList()
        return sqlVideoList.value!!.filter { !serverVideoList.value!!.contains(it)}
    }


    private fun findNotDownloadedVideos(): List<Video> {
        if (serverVideoList.value == null || serverVideoList.value!!.isEmpty()) return emptyList()
        if (sqlVideoList.value == null || sqlVideoList.value!!.isEmpty()) return serverVideoList.value ?: emptyList()
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



    inner class ServerController {
        fun getJwt(): Boolean {
            connectionState.postValue(ConnectionState.WORKING)
            val response = serverRepository.getJwt(User("foo", "foo")).execute()
            if (response.isSuccessful) {
                RetrofitInstance.jwt = JSONObject(response.body().toString()).getString("jwt")
                return true
            } else {
                return false
            }
        }

        fun connect(): Boolean {
            try {
                serverController.getJwt()
            } catch (e: Exception) {
                e.printStackTrace()
                for (url in sqlUrlList.value ?: emptyList()) {
                    serverController.changeUrl(url.url)
                    try {
                        if (serverController.getJwt()) return true
                    } catch (e: Exception) { e.printStackTrace() }
                }
                connectionState.postValue(ConnectionState.ERROR)
                e.printStackTrace()
                return false
            }
            return true
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

        suspend fun downloadVideo(video: Video) {
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
                val file = File( "$filesDir${File.separator}$fileName")
                if (file.length() != 0L)file.delete()

                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null

                try {
                    val fileReader = ByteArray(4096)
                    val fileSize = body.contentLength()
                    var fileSizeDownloaded: Long = 0
                    var fileDownloadedPercent = 0

                    inputStream = body.byteStream()
                    outputStream = FileOutputStream(file)

                    while (true) {
                        val read = inputStream.read(fileReader)
                        if (read == -1)break
                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded+=read
                        if (fileSizeDownloaded >= fileSize/100*fileDownloadedPercent){
                            Log.d(TAG, "writeResponseBodyToDisk: File download: $fileDownloadedPercent%")
                            fileDownloadedPercent++
//                            Log.d(TAG, "writeResponseBodyToDisk: File download: $fileSizeDownloaded of $fileSize")
                        }
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

        fun changeUrl(url: String) {
            Log.d(TAG, "changeUrl: To $url")
            RetrofitInstance.setNewUrl(url)
        }

        suspend fun getUrlList(): List<Url> {
            val response = serverRepository.getUrlList()
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            }
            return emptyList()
        }

    }


    inner class SqlController {

        suspend fun addVideo(video: Video) {
            sqlRepository.addVideo(video)
        }

        suspend fun deleteVideo(video: Video) {
            sqlRepository.deleteVideo(video)
        }

        suspend fun addUrl(url: Url){
            sqlRepository.addUrl(url)
        }

        suspend fun replaceUrlList(serverUrlList: List<Url>) {
            sqlRepository.deleteAllUrl()
            for (url in serverUrlList){
                sqlRepository.addUrl(url)
            }
        }
    }


}

