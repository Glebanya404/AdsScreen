package ru.customelectronics.adsscreen.repository

import androidx.lifecycle.LiveData
import ru.customelectronics.adsscreen.model.Video
import ru.customelectronics.adsscreen.room.VideoDao

class SqlRepository(private val videoDao: VideoDao) {
    val getAll: LiveData<List<Video>> = videoDao.getAll()

    suspend fun addVideo(video: Video){
        videoDao.insertAll(video)
    }

    suspend fun delete(video: Video) {
        videoDao.delete(video)
    }
}

