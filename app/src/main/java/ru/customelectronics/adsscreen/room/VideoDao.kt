package ru.customelectronics.adsscreen.room

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.customelectronics.adsscreen.model.Video

@Dao
interface VideoDao {

    @Query("SELECT * FROM video_table ORDER BY id ASC")
    fun getAll(): LiveData<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg  videos: Video)

    @Delete
    fun delete(video: Video)
}