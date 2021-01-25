package ru.customelectronics.adsscreen.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

@Entity(tableName = "video_table")
data class Video(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val title: String,
    val fileName: String,
    val dateOfUpload: String
){
    override fun equals(other: Any?): Boolean {
        if (other is Video) return this.id == other.id
        else return super.equals(other)
    }
}