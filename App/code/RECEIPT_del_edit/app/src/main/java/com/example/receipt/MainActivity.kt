package com.example.receipt


import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate
import com.example.receipt.databinding.ActivityMainBinding
import com.example.receipt.edit.EditActivity
import kotlinx.android.synthetic.main.activity_main.rv_profile
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import com.example.receipt.room.Mylist
import com.example.receipt.room.MylistAdapter
import com.example.receipt.room.MylistDB
import kotlinx.android.synthetic.main.list_item.*
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    //var profileList = ArrayList<Profiles>()
    val today: LocalDate = LocalDate.now()
    val displayList = ArrayList<Mylist>()


    //현재 보유 품목
    private var mylistDb : MylistDB? =null
    private var myList = listOf<Mylist>() //mtlistDb에 있는 내용 mylist에 저장
    //lateinit var mylistAdapter : MylistAdapter




    val Iconimage= listOf<Int>(R.drawable.icon_1, R.drawable.icon_2,R.drawable.icon_3,
        R.drawable.icon_4,R.drawable.icon_5, R.drawable.icon_6,R.drawable.icon_7,
        R.drawable.icon_8, R.drawable.icon_9,R.drawable.icon_10)

    fun jsonParsing(): JSONArray {
        var jsonString = assets.open("json/data.json").reader().readText()
        val jsonArray = JSONArray(jsonString)

        return jsonArray
    }

    /**

    companion object{
        private var instance:MainActivity? = null
        fun getInstance(): MainActivity? {
            return instance
        }
    }
    **/

    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        //var itemList = intent.getSerializableExtra("OCR") as ArrayList<Int>?

       //현재 보유 품목 mylistDB(room)에 있는 내용 다 들고와서 리사이클러 뷰로 나타내기



        val r = Runnable {
            try {
                mylistDb = MylistDB.getInstance(this)
                myList= mylistDb?.mylistDao()?.getAll()!!// mylist에 있는 내용 다 들고옴
                //데이터 읽고 쓸 떄는 쓰레드 사용
                Log.d("myList", "${myList.size}")
                displayList.addAll(myList)

                //val mylistAdapter = MylistAdapter(this, displayList)
                //mylistAdapter.notifyDataSetChanged()
                //rv_profile.adapter = mylistAdapter
                //rv_profile.layoutManager = LinearLayoutManager(this)
                //rv_profile.setHasFixedSize(true)
                val mylistAdapter =MylistAdapter(this, displayList)
                mylistAdapter.setOnItemClickListener(object: MylistAdapter.OnItemClickListener {

                    override fun onItemEditClick(data: Mylist, pos: Int) {

                        /**
                        myList.name = "123123"
                        mylistAdapter.updateItem(pos, data)
                        mylistAdapter.notifyItemRangeChanged(pos, 1)
                        **/
                    }

                    override fun onItemDeleteClick(data:Mylist, position: Int) {
                        /**
                        Log.d("item 삭제", data.toString())
                        mylistAdapter.deleteItem(position)
                        mylistAdapter.notifyItemRemoved(position)
                        mylistAdapter.notifyItemRangeChanged(position, displayList.size)
                        //mylistDb?.mylistDao()?.delete(position)

                        //rv_profile.adapter?.notifyDataSetChanged()
                        **/
                    }
                })

                mBinding.rvProfile.adapter = mylistAdapter
                mBinding.rvProfile.addItemDecoration(DistanceItemDecorator(10))
            }catch(e:Exception){
                Log.d("tag","Error - $e")
            }
        }
        val thread = Thread(r)
        thread.start()


        btn_cam.setOnClickListener {
            val intent = Intent(this, OCRActivity::class.java)
            startActivity(intent)
            finish()
        }




        // OCR Activity에서 넘겨받은 index를 리사이클러 뷰로 나타내기 위함
        /**
        if (itemList != null) {
            val today: LocalDate = LocalDate.now()
            for (i in itemList!!){
                val jsonObject = jsonParsing().getJSONObject(i)
                val name = jsonObject.getString("Food")
                val cate = jsonObject.getString("Category")
                val day = jsonObject.getString("ExpirationDate")

                profileList.add(Profiles(Iconimage[i], name, cate, today.plusDays(day.toLong())))
            }
        }
        displayList.addAll(profileList)
        rv_profile.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_profile.setHasFixedSize(true)
        rv_profile.adapter = ProfileAdapter(profileList)
        **/


    }

    class DistanceItemDecorator(private val value: Int): RecyclerView.ItemDecoration(){
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.top = value
            outRect.left = value
            outRect.bottom = value
            outRect.right = value
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val menuItem = menu?.findItem(R.id.search)
        //if (menuItem != null) {
        val searchView = menuItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }


            /**
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()) {
                    displayList.clear()
                    val search = newText.toLowerCase(Locale.getDefault())
                    myList.forEach {
                        if (it.name.toLowerCase(Locale.getDefault()).contains(search)) {
                            displayList.add(it)
                        }
                    }
                    rv_profile.adapter!!.notifyDataSetChanged()
                } else {
                    displayList.clear()
                    displayList.addAll(myList)
                    rv_profile.adapter!!.notifyDataSetChanged()
                }
                return false
            }**/

        })

        rv_profile.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_profile.setHasFixedSize(true)
        rv_profile.adapter = MylistAdapter(this, displayList)

        return super.onCreateOptionsMenu(menu)
        //}
    }
    override fun onDestroy() {
        MylistDB.destroyInstance()
        mylistDb = null
        super.onDestroy()
    }



}