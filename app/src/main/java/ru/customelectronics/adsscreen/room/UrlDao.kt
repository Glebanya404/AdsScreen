package ru.customelectronics.adsscreen.room

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.customelectronics.adsscreen.model.Url

@Dao
interface UrlDao {

    @Query("SELECT * FROM url_table ORDER BY `order` ASC")
    fun getAll(): LiveData<List<Url>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(url: Url)

    @Delete
    fun delete(url: Url)

    @Query("DELETE FROM url_table")
    fun deleteAll()
}