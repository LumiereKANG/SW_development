package com.example.receipt.ocr

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.receipt.R
import com.example.receipt.possetionList.MyListDb
import com.example.receipt.recycle.ListExtractView
import com.example.receipt.recycle.MyList
import org.json.JSONArray
import java.io.*
import java.time.LocalDate

class OcrClass : AppCompatActivity(){

    private var myListDb: MyListDb? = null

    private val iconImage = listOf<Int>(
        R.drawable.icon_1, R.drawable.icon_2, R.drawable.icon_3,
        R.drawable.icon_4, R.drawable.icon_5, R.drawable.icon_6, R.drawable.icon_7,
        R.drawable.icon_8, R.drawable.icon_9, R.drawable.icon_10
    )

    fun checkFile(dataPath: String, lang: String, assetManager: AssetManager) {
        var dir = File(dataPath + "tessdata/")

        if (!dir.exists() && dir.mkdirs()) {
            copyFile(dataPath, lang, assetManager)
        }
        if (dir.exists()) {
            val datafilePath: String = dataPath + "/tessdata/" + lang + "traineddata"
            val dataFile = File(datafilePath)
            if (!dataFile.exists()) {
                copyFile(dataPath, lang, assetManager)
            }
        }
    }

    fun copyFile(dataPath: String, lang: String, assetManager: AssetManager) {
        try {

            val filePath: String = dataPath + "/tessdata/" + lang + ".traineddata"

            //AssetManager를 사용하기 위한 객체 생성
            //byte 스트림을 읽기 쓰기용으로 열기

            val inputStream: InputStream = assetManager.open("tessdata/" + lang + ".traineddata")
            val outStream: OutputStream = FileOutputStream(filePath)


            //위에 적어둔 파일 경로쪽으로 해당 바이트코드 파일을 복사한다.
            val buffer = ByteArray(1024)

            var read: Int
            read = inputStream.read(buffer)
            while (read != -1) {
                outStream.write(buffer, 0, read)
                read = inputStream.read(buffer)
            }
            outStream.flush()
            outStream.close()
            inputStream.close()

        } catch (e: FileNotFoundException) {
            Log.v("오류발생", e.toString())
        } catch (e: IOException) {
            Log.v("오류발생", e.toString())
        }
    }


    fun ocrExtract(ocrResult: String, jsonArray: JSONArray): List<Int> {
        val ocrTextList = ocrResult.split("\\n")
        val extractIndex = textExtract(ocrTextList, jsonArray)

        return extractIndex
    }


    fun textExtract(ocrTextList: List<String>, jsonArray: JSONArray): List<Int> {
        val itemName = mutableListOf<String>()

        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)
            val name = jsonObject.getString("Food")
            itemName.add(name)
        }

        val extractIndex = ArrayList<Int>()

        for ((index, value) in itemName.withIndex()) {
            val displaying = ocrTextList.filter { X -> X.contains(value) }
            if (displaying.isNotEmpty()) {
                extractIndex.add(index)
            }
        }
        return extractIndex
    }

    fun jsonParsing(extractIndex : ArrayList<Int>, jsonArray: JSONArray): ArrayList<ListExtractView> {
        val today: LocalDate = LocalDate.now()
        val ocrViewlist = ArrayList<ListExtractView>()
        for (i in extractIndex) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.getString("Food")
            val cate = jsonObject.getString("Category")
            val day = jsonObject.getString("Refri_Date")

            ocrViewlist.add(ListExtractView(iconImage[i], name, cate, today.plusDays(day.toLong())))
         }

        return ocrViewlist
    }

    fun insertDB_item(extractIndex: ArrayList<Int>, jsonArray: JSONArray, context: Context){
        val today: LocalDate = LocalDate.now()
        val newitem = MyList()

        for (i in extractIndex) {
            val jsonObject = jsonArray.getJSONObject(i)
            newitem.item = iconImage[i]
            newitem.name = jsonObject.getString("Food")
            newitem.category = jsonObject.getString("Category")
            newitem.index =  jsonObject.getInt("Index")

            val day = jsonObject.getString("Refri_Date")
            newitem.dates = today.plusDays(day.toLong()).toString()

            myListDb = MyListDb.getInstance(context)
            myListDb?.myListDao()?.insert(newitem)
        }
    }
}