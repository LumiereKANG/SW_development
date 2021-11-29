package com.example.receipt.possetionList

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.example.receipt.recycle.MyList

@Dao
interface MyListDao {
    @Query("SELECT * FROM mylist")
    fun getAll(): List<MyList>

    @Insert(onConflict = REPLACE)
    fun insert(mylist: MyList)

    @Delete
    fun delete(mylist: MyList)

    @Update
    fun update(mylist: MyList)
}