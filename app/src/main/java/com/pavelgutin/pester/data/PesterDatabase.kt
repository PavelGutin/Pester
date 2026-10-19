package com.pavelgutin.pester.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NagEntity::class], version = 1, exportSchema = true)
abstract class PesterDatabase : RoomDatabase() {
    abstract fun nagDao(): NagDao

    companion object {
        @Volatile private var instance: PesterDatabase? = null

        fun get(context: Context): PesterDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PesterDatabase::class.java,
                    "pester.db"
                ).build().also { instance = it }
            }
    }
}
