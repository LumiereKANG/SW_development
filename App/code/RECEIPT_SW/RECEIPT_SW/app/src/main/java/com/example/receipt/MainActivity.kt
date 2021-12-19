package com.example.receipt


import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.receipt.databinding.ActivityMainBinding
import com.example.receipt.ocr.ListExtratActivity
import com.example.receipt.possetionList.MyListDb
import com.example.receipt.recycle.MyListAdapter
import com.example.receipt.recycle.MyList
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val displayList = ArrayList<MyList>()

    //현재 보유 품목
    private var mylistDb : MyListDb? =null
    private var myList = listOf<MyList>()

    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val r = Runnable {
            try {
                mylistDb = MyListDb.getInstance(this)
                myList= mylistDb?.myListDao()?.getAll()!!// mylist에 있는 내용 다 들고옴
                //데이터 읽고 쓸 떄는 쓰레드 사용
                Log.d("myList", "${myList.size}")
                displayList.addAll(myList)

                val mylistAdapter = MyListAdapter(this, displayList)
                mylistAdapter.setOnItemClickListener(object: MyListAdapter.OnItemClickListener {
                    override fun onItemEditClick(data: MyList, pos: Int) {
                    }
                    override fun onItemDeleteClick(data: MyList, position: Int) {
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
            val intent = Intent(this, ListExtratActivity::class.java)
            startActivity(intent)
            finish()
        }
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
        val r = Runnable {
            try {
                mylistDb = MyListDb.getInstance(this)
                myList= mylistDb?.myListDao()?.getAll()!!// mylist에 있는 내용 다 들고옴
            }catch(e:Exception){
                Log.d("tag","Error - $e")
            }
        }

        menuInflater.inflate(R.menu.menu, menu)
        val menuItem = menu?.findItem(R.id.search)
        val searchView = menuItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val thread = Thread(r)
                if (newText!!.isNotEmpty()) {
                    displayList.clear()
                    val search = newText.lowercase(Locale.getDefault())
                    thread.start()
                    myList.forEach {
                        if (it.name.lowercase(Locale.getDefault()).contains(search)) {
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
            }
        })

        rv_profile.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_profile.setHasFixedSize(true)
        rv_profile.adapter = MyListAdapter(this, displayList)

        return super.onCreateOptionsMenu(menu)
    }
    override fun onDestroy() {
        MyListDb.destroyInstance()
        mylistDb = null
        super.onDestroy()
    }

}