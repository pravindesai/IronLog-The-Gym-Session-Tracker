package com.gympilot.ironlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WorkoutPlan::class, Exercise::class, WorkoutSession::class, WorkoutLog::class],
    version = 1,
    exportSchema = true
)
abstract class IronLogDatabase : RoomDatabase() {
    abstract fun dao(): IronLogDao

    companion object {
        @Volatile
        private var instance: IronLogDatabase? = null

        fun getInstance(context: Context): IronLogDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IronLogDatabase::class.java,
                    "ironlog.db"
                ).build().also { instance = it }
            }
        }
    }
}
