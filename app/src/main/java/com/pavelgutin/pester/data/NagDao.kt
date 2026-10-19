package com.pavelgutin.pester.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NagDao {
    @Query("SELECT * FROM nags WHERE type = :type ORDER BY hour, minute")
    fun observeByType(type: NagType): Flow<List<NagEntity>>

    @Query("SELECT * FROM nags WHERE enabled = 1")
    suspend fun getAllEnabled(): List<NagEntity>

    @Query("SELECT * FROM nags WHERE id = :id")
    suspend fun getById(id: Long): NagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(nag: NagEntity): Long

    @Update
    suspend fun update(nag: NagEntity)

    @Delete
    suspend fun delete(nag: NagEntity)
}
