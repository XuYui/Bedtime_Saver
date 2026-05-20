package com.bedtimesaver.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DailySleepRecord::class],
    version = 1,
    exportSchema = false,
)
abstract class BedtimeDatabase : RoomDatabase() {
    abstract fun sleepRecordDao(): SleepRecordDao

    companion object {
        @Volatile
        private var instance: BedtimeDatabase? = null

        fun getInstance(context: Context): BedtimeDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BedtimeDatabase::class.java,
                    "bedtime_saver.db",
                ).build().also { instance = it }
            }
        }
    }
}
