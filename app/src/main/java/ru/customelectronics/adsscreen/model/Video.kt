package ru.customelectronics.adsscreen.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Video(
    val id: Long,
    val title: String,
    val fileName: String,
    val dateOfUpload: String
)