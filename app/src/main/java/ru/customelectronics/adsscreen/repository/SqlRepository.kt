package ru.customelectronics.adsscreen.repository

import androidx.lifecycle.LiveData
import ru.customelectronics.adsscreen.model.Url
import ru.customelectronics.adsscreen.model.Video
import ru.customelectronics.adsscreen.room.UrlDao
import ru.customelectronics.adsscreen.room.VideoDao

class SqlRepository(private val videoDao: VideoDao,private val urlDao: UrlDao) {
    val getAllVideos: LiveData<List<Video>> = videoDao.getAll()
    val getAllUrls: LiveData<List<Url>> = urlDao.getAll()

    suspend fun addVideo(video: Video){
        videoDao.insertAll(video)
    }

    suspend fun deleteVideo(video: Video) {
        videoDao.delete(video)
    }



    suspend fun addUrl(url: Url) {
        urlDao.insert(url)
    }

    suspend fun deleteUrl(url: Url) {
        urlDao.delete(url)
    }

    fun deleteAllUrl() {
        urlDao.deleteAll()
    }

}

