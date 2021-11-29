package com.example.receipt.recycle

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.receipt.R
import com.example.receipt.edit.EditActivity
import kotlinx.android.synthetic.main.mylist_item.view.*
import java.io.Serializable
import android.content.Intent
import com.example.receipt.possetionList.MyListDb
import org.antlr.v4.runtime.misc.MurmurHash.finish
import kotlin.collections.ArrayList


class MyListAdapter(private val context: Context, private val displaylist: ArrayList<MyList>):
 RecyclerView.Adapter<MyListAdapter.Holder>(){

 interface OnItemClickListener{
  fun onItemEditClick(data: MyList, position: Int)
  fun onItemDeleteClick(data: MyList, position: Int)
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
  view = LayoutInflater.from(parent.context).inflate(R.layout.mylist_item, parent, false)
  return Holder(view)
 }

 override fun onBindViewHolder(holder: Holder, position: Int) {

  holder.run{

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
 }

 inner class Holder(view: View) : RecyclerView.ViewHolder(view){

  val itemview = itemView?.findViewById<ImageView>(R.id.iv_profile)

  private var mview: View = view
  fun bind(mylist : MyList){
   if (itemview != null) {
    Glide.with(itemView).load(mylist.item).into(itemview)
   }
   mview.tv_name.text = mylist.name
   mview.tv_kategorie.text = mylist.category
   mview.tv_dates.text = mylist.dates
  }
 }

 fun deleteItem(pos: Int){
  val rt = Runnable{ MyListDb.getInstance(context)?.myListDao()?.delete(displaylist[pos])!!}
  val thread = Thread(rt)

  if (displaylist != null){
   thread.start()
   displaylist.removeAt(pos)
   notifyItemRemoved(pos)
   notifyItemRangeChanged(pos,displaylist.size)
  }
  else {
  }
 }

 fun editItem(pos: Int){
  val intent = Intent(context, EditActivity::class.java)
  intent.putExtra("item", displaylist[pos] as Serializable)
  context.startActivity(intent)
 }
}

