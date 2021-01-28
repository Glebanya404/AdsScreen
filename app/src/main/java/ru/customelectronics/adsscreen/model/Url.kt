package ru.customelectronics.adsscreen.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "url_table")
data class Url(
        @PrimaryKey(autoGenerate = true)
        val id: Long? = null,
        val url: String,
        val order: Int
) {

}
