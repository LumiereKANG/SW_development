package com.example.receipt.possetionList

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.receipt.recycle.MyList


@Database(entities = [MyList::class], version = 2)
abstract class MyListDb: RoomDatabase() {
    abstract fun myListDao(): MyListDao

    companion object {
        private var INSTANCE: MyListDb? = null

        fun getInstance(context: Context): MyListDb? {
            if (INSTANCE == null) {
                synchronized(MyListDb::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        MyListDb::class.java, "mylist.db")
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