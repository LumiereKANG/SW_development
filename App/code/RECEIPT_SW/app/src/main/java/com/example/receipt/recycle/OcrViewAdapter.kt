package com.example.receipt.recycle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.receipt.R

class OcrViewAdapter(val profileList: MutableList<OcrView>) : RecyclerView.Adapter<OcrViewAdapter.CustomViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ocrview_item, parent, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return profileList.size
    }


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {//각각을 연결해주는 곳
        holder.item.setImageResource(profileList.get(position).item)
        holder.name.text = profileList.get(position).name
        holder.categories.text = profileList.get(position).categories
        holder.dates.text = profileList.get(position).dates.toString()
    }

    class CustomViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val item = itemView.findViewById<ImageView>(R.id.iv_profile)         //item 사진 관련
        val name = itemView.findViewById<TextView>(R.id.tv_name)             //품목명
        val categories = itemView.findViewById<TextView>(R.id.tv_kategorie)   //카테고리
        val dates = itemView.findViewById<TextView>(R.id.tv_dates)           //소비기한


    }

}