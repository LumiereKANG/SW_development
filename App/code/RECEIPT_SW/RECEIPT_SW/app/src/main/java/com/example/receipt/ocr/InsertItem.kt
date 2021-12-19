package com.example.receipt.ocr

import android.content.Context
import com.example.receipt.R
import com.example.receipt.possetionList.MyListDb
import com.example.receipt.recycle.ListExtractView
import com.example.receipt.recycle.MyList
import org.json.JSONArray
import java.time.LocalDate

class InsertItem {

    private var myListDb: MyListDb? = null

    private val iconImage = listOf<Int>(
        R.drawable.icon_1, R.drawable.icon_2, R.drawable.icon_3,
        R.drawable.icon_4, R.drawable.icon_5, R.drawable.icon_6, R.drawable.icon_7,
        R.drawable.icon_8, R.drawable.icon_9, R.drawable.icon_10,R.drawable.icon_11,
        R.drawable.icon_12,R.drawable.icon_13, R.drawable.icon_14, R.drawable.icon_15,
        R.drawable.icon_16, R.drawable.icon_17,
        R.drawable.icon_18, R.drawable.icon_19, R.drawable.icon_20,R.drawable.icon_21,
        R.drawable.icon_22,R.drawable.icon_23, R.drawable.icon_24, R.drawable.icon_25,
    )

    fun jsonParsing(extractIndex : ArrayList<Int>, jsonArray: JSONArray): ArrayList<ListExtractView> {
        val today: LocalDate = LocalDate.now()
        val ocrViewlist = ArrayList<ListExtractView>()
        for (i in extractIndex) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("Food")
            val cate = jsonObject.getString("Category")
            val day = jsonObject.getString("Refri_Date")

            if (i<26) {
                ocrViewlist.add(ListExtractView(iconImage[i], name, cate, today.plusDays(day.toLong())))
            }
            else {
                ocrViewlist.add(ListExtractView(R.drawable.icon, name, cate, today.plusDays(day.toLong())))
            }

        }

        return ocrViewlist
    }

    fun insertDB_item(extractIndex: ArrayList<Int>, jsonArray: JSONArray, context: Context){
        val today: LocalDate = LocalDate.now()
        val newitem = MyList()

        for (i in extractIndex) {
            val jsonObject = jsonArray.getJSONObject(i)
            if (i<26) {
                newitem.item = iconImage[i]
            }
            else {
                newitem.item = R.drawable.icon
            }

            newitem.name = jsonObject.getString("Food")
            newitem.category = jsonObject.getString("Category")
            newitem.index =  jsonObject.getInt("Index")

            val day = jsonObject.getString("Refri_Date")
            newitem.dates = today.plusDays(day.toLong()).toString()
            newitem.startday = today.toString()
            newitem.storage = "냉장보관 중"

            myListDb = MyListDb.getInstance(context)
            myListDb?.myListDao()?.insert(newitem)
        }
    }
}