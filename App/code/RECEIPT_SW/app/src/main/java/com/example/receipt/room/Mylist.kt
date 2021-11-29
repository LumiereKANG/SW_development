package com.example.receipt.room
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDate


@Entity(tableName = "mylist")
class Mylist(@PrimaryKey var id: Long?,
          @ColumnInfo(name = "item") var item: Int,
          @ColumnInfo(name = "name") var name: String,
          @ColumnInfo(name = "categories") var category: String,
          @ColumnInfo(name = "dates") var dates: String,
          @ColumnInfo(name = "index") var index: Int

): Serializable
{
        constructor(): this(null,0,"","","",0)
    //초기값
}
