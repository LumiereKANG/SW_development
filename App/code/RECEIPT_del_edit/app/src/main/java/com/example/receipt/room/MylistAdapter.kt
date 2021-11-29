package com.example.receipt.room

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.receipt.MainActivity
import com.example.receipt.OCRActivity
import com.example.receipt.R
import com.example.receipt.edit.EditActivity
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.list_item.view.*
import java.io.Serializable
import java.security.AccessController.getContext
import android.content.Intent


class MylistAdapter(private val context: Context, private val displaylist: ArrayList<Mylist>):
 RecyclerView.Adapter<MylistAdapter.Holder>(){



 interface OnItemClickListener{
  fun onItemEditClick(data: Mylist, position: Int)
  fun onItemDeleteClick(data: Mylist, position: Int)
 }

 private var clickListener : OnItemClickListener? = null
 fun setOnItemClickListener(listener: OnItemClickListener){
  this.clickListener = listener
  Log.d("setClick", this.clickListener.toString())
 }


 override fun getItemCount(): Int {
  return displaylist.size
 }

 override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
  val view: View?
  view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
  return Holder(view)
 }

 override fun onBindViewHolder(holder: Holder, position: Int) {
  holder.run{
   itemView.tag = displaylist[position]

   itemView.del_btn?.setOnClickListener{
    clickListener?.onItemDeleteClick(displaylist[position],position)
    deleteItem(position)
   }

   itemView.edit_btn?.setOnClickListener{
    clickListener?.onItemEditClick(displaylist[position],position)
    editItem(position)
   }
  }
  holder.bind(displaylist[position])



  /**
  val item = displaylist[position]
  val clickListener = View.OnClickListener { it ->
   if(position!= RecyclerView.NO_POSITION){
    clicklistener?.onItemDeleteClick(item,position)
   }

  } **/

 }

 inner class Holder(view: View) : RecyclerView.ViewHolder(view){

  val itemview = itemView?.findViewById<ImageView>(R.id.iv_profile)         //item 사진 관련
  /**
  val nameview = itemView?.findViewById<TextView>(R.id.tv_name)             //품목명
  val categoriesview = itemView?.findViewById<TextView>(R.id.tv_kategorie)   //카테고리
  val datesview = itemView?.findViewById<TextView>(R.id.tv_dates)           //소비기한

  **/

  private var mview: View = view
  fun bind(mylist : Mylist){
   if (itemview != null) {
    Glide.with(itemView).load(mylist.item).into(itemview)
   }
   mview.tv_name.text = mylist.name
   mview.tv_kategorie.text = mylist.category
   mview.tv_dates.text = mylist.dates
  }

 }

 fun deleteItem(pos: Int){
  val rt = Runnable{MylistDB?.getInstance(context)?.mylistDao()?.delete(displaylist[pos])!!}
  val thread = Thread(rt)

  thread.start()

  if (displaylist != null){
   Log.d("진짜삭제된 품목명", displaylist[pos].name)
   displaylist.removeAt(pos)
   notifyItemRemoved(pos)
   notifyItemRangeChanged(pos,displaylist.size)
   //Log.d("삭제사이즈",displaylist.size.toString())
   Log.d("진짜삭제성공",pos.toString())

  }
  else{
  }
 }

 fun editItem(pos: Int){

  val intent = Intent(context, EditActivity::class.java)
  intent.putExtra("item", displaylist[pos] as Serializable)
  context.startActivity(intent)
 }
}

