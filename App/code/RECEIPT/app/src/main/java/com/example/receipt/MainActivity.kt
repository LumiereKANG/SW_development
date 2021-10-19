package com.example.receipt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.rv_profile
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    var profileList = ArrayList<Profiles>()
    val today: LocalDate = LocalDate.now()
    val displayList = ArrayList<Profiles>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var itemList = intent.getSerializableExtra("OCR") as ArrayList<String>?

        btn_cam.setOnClickListener {
            val intent = Intent(this, OCRActivity::class.java)
            startActivity(intent)
        }
        //profileList.add(Profiles(R.drawable.orange, "사과", "과일", today))

        if (itemList != null) {
            for (i in itemList!!) {
                profileList.add(Profiles(R.drawable.orange, i, "과일", today))
            }
        }
        displayList.addAll(profileList)
        rv_profile.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_profile.setHasFixedSize(true)
        rv_profile.adapter = ProfileAdapter(profileList)

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

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.isNotEmpty()) {
                        displayList.clear()
                        val search = newText.toLowerCase(Locale.getDefault())
                        profileList.forEach {
                            if (it.name.toLowerCase(Locale.getDefault()).contains(search)) {
                                displayList.add(it)
                            }
                        }
                        rv_profile.adapter!!.notifyDataSetChanged()
                    } else {
                        displayList.clear()
                        displayList.addAll(profileList)
                        rv_profile.adapter!!.notifyDataSetChanged()
                    }
                    return false
                }
            })
        rv_profile.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_profile.setHasFixedSize(true)
        rv_profile.adapter = ProfileAdapter(displayList)

            return super.onCreateOptionsMenu(menu)
        //}




    }
}