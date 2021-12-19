package com.example.receipt.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/* CatDB.kt */

@Database(entities = [Mylist::class], version = 1)
abstract class MylistDB: RoomDatabase() {
    abstract fun mylistDao(): MylistDao

    companion object {
        private var INSTANCE: MylistDB? = null

        fun getInstance(context: Context): MylistDB? {
            if (INSTANCE == null) {
                synchronized(MylistDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        MylistDB::class.java, "cat.db")
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}