package com.example.sotukenv2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Favorite::class, Setting::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
    abstract fun settingDao(): SettingDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if(instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app-database"
                    )
                        .createFromAsset("app-database.db")
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

}
