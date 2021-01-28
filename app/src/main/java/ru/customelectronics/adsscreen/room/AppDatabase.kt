package ru.customelectronics.adsscreen.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.customelectronics.adsscreen.model.Url
import ru.customelectronics.adsscreen.model.Video

@Database(entities = [Video::class, Url::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun urlDao(): UrlDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "video_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}