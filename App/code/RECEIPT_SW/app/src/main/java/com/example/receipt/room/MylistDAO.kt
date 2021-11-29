package com.example.receipt.room

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.example.receipt.room.Mylist

@Dao
interface MylistDao {
    @Query("SELECT * FROM mylist")
    fun getAll(): List<Mylist>

    /* import android.arch.persistence.room.OnConflictStrategy.REPLACE */
    @Insert(onConflict = REPLACE)
    fun insert(mylist: Mylist)

    //@Query("DELETE from mylist")
    //fun deleteAll(): List<Mylist>

    @Delete
    fun delete(mylist:Mylist)
    @Update
    fun update(mylist: Mylist)
}