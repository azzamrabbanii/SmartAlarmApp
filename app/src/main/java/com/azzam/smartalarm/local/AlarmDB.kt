package com.azzam.smartalarm.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.azzam.smartalarm.data.Alarm

@Database(entities = [Alarm::class], version = 2)
abstract class AlarmDB: RoomDatabase() {
    abstract fun alarmDao() : AlarmDao

    companion object {
        @Volatile
        var instance: AlarmDB? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: builtDataBase(context).also {
                instance = it
            }
        }

        private fun builtDataBase(context: Context) =
            Room.databaseBuilder(context, AlarmDB::class.java, "smart_alarm.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}