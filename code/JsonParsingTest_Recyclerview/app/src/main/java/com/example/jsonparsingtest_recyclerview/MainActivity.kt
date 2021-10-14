package com.example.jsonparsingtest_recyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jsonparsingtest_recyclerview.databinding.ActivityMainBinding
import com.google.gson.Gson
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val data = getJsonData("FoodDate_cold.json")

        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = MyRecyclerviewAdapter(data!!)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun getJsonData(filename: String): FoodExpirationDate? {
        val assetManager = resources.assets // asset 파일 접근을 하기 위함
        var result: FoodExpirationDate? = null // 함수에서 반환해 줄 result 변수
        try{
            val inputStream = assetManager.open(filename) // 전달받은 파일이름으로 파일 open
            val reader = inputStream.bufferedReader() // 내용을 binary 형태로 읽어와서(inputStream) Text로 변환(bufferedReader)
            val gson = Gson()
            result = gson.fromJson(reader, FoodExpirationDate::class.java) // 읽어온 text data를 FoodExpirationDate 클래스로 변환(형태 보존)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }
}