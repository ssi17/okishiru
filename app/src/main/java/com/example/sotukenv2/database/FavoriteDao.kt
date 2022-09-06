package com.example.sotukenv2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite")
    suspend fun getAll(): List<Favorite>

    @Query("UPDATE favorite SET flag = :flag WHERE id = :id")
    suspend fun changeFlag(id: Int, flag: Boolean)

    @Insert
    suspend fun addFlag(obj: Favorite)
}