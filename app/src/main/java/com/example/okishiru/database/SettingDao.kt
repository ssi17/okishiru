package com.example.okishiru.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SettingDao {
    @Query("SELECT * FROM setting")
    suspend fun getAll(): List<Setting>

    @Query("SELECT flag FROM setting WHERE name = :name")
    suspend fun getFlag(name: String): Int

    @Query("UPDATE setting SET flag = :flag WHERE name = :name")
    suspend fun changeFlag(name: String, flag: Int)
}