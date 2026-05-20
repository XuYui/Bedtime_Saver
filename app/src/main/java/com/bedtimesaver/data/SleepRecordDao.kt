package com.bedtimesaver.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepRecordDao {
    @Query("SELECT * FROM daily_sleep_records ORDER BY date DESC")
    fun observeRecords(): Flow<List<DailySleepRecord>>

    @Query("SELECT * FROM daily_sleep_records WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailySleepRecord?

    @Query("SELECT * FROM daily_sleep_records ORDER BY date DESC")
    suspend fun getAllOnce(): List<DailySleepRecord>

    @Query("DELETE FROM daily_sleep_records WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Upsert
    suspend fun upsert(record: DailySleepRecord)
}
