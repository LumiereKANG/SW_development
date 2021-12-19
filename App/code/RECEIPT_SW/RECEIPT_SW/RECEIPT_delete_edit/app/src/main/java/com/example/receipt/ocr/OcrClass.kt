package com.example.receipt.ocr

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.receipt.R
import com.example.receipt.recycle.OcrView
import com.googlecode.tesseract.android.TessBaseAPI
import org.json.JSONArray
import java.io.*
import java.time.LocalDate

class OcrClass : AppCompatActivity() {

    private val iconImage = listOf<Int>(
        R.drawable.icon_1, R.drawable.icon_2, R.drawable.icon_3,
        R.drawable.icon_4, R.drawable.icon_5, R.drawable.icon_6, R.drawable.icon_7,
        R.drawable.icon_8, R.drawable.icon_9, R.drawable.icon_10
    )

    /**
     * 키워드 추출
     */

    fun jsonParsing_array(): JSONArray {
        val jsonString = assets.open("json/data.json").reader().readText()
        return JSONArray(jsonString)
    }

    fun textExtract(extractAllingre: List<String>, jsonArray: JSONArray): List<Int> {
        val itemName = mutableListOf<String>()

        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            val name = jsonObject.getString("Food")
            itemName.add(name)
        }

        val indexlist = ArrayList<Int>()

        for ((index, value) in itemName.withIndex()) {
            val displaying = extractAllingre.filter { X -> X.contains(value) }
            if (displaying.isNotEmpty()) {
                indexlist.add(index)
            }
        }
        return indexlist
    }

    fun ocrExtract(ocr_text: String): List<Int> {
        val ocrList = ocr_text.split("\\n")
        val resultList = textExtract(ocrList, jsonParsing_array())
        return resultList
    }

    fun jsonParsing(extractIndex: ArrayList<Int>): ArrayList<OcrView> {

        val jsonArray =  jsonParsing_array()

        val today: LocalDate = LocalDate.now()
        val profilelist = ArrayList<OcrView>()
        for (i in extractIndex) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("Food")
            val cate = jsonObject.getString("Category")
            val day = jsonObject.getString("ExpirationDate")

            profilelist.add(OcrView(iconImage[i], name, cate, today.plusDays(day.toLong())))
        }

        return profilelist
    }
}