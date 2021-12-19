package com.example.receipt.ocr

import android.content.res.AssetManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.io.*

class Ocr : AppCompatActivity(){


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
}