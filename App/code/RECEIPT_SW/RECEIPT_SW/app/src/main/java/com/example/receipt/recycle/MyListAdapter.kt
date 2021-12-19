package com.example.receipt.recycle

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.receipt.R
import kotlinx.android.synthetic.main.mylist_item.view.*
import java.io.Serializable
import android.content.Intent
import android.widget.Toast
import com.example.receipt.Delete
import com.example.receipt.EditActivity

import kotlin.collections.ArrayList


class MyListAdapter(private val context: Context, private val displaylist: ArrayList<MyList>):
 RecyclerView.Adapter<MyListAdapter.Holder>(){

 private var delete = Delete()


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
    Log.d("삭제 전 사이즈",displaylist.size.toString())
    Log.d("클릭된 삭제 포지션",position.toString())

    clickListener?.onItemDeleteClick(displaylist[position],position)
    deleteRecycle(position)
   }

   itemView.edit_btn?.setOnClickListener(){
    clickListener?.onItemEditClick(displaylist[position],position)
    editItem(position)
   }
  }
  holder.bind(displaylist[position])
 }

 inner class Holder(view: View) : RecyclerView.ViewHolder(view){

  val itemview = itemView?.findViewById<ImageView>(R.id.iv_profile)         //item 사진 관련

  private var mview: View = view
  fun bind(mylist : MyList){
   if (itemview != null) {
    Glide.with(itemView).load(mylist.item).into(itemview)
   }
   mview.tv_name.text = mylist.name
   mview.tv_kategorie.text = mylist.category
   mview.tv_dates.text = mylist.dates
   mview.tv_stor.text = mylist.storage
  }

 }

 fun deleteRecycle(pos: Int){
  var builder = AlertDialog.Builder(context)
  builder.setMessage(displaylist[pos].name + "을 삭제 하시겠습니까?")
  //builder.setIcon(R.mipmap.ic_launcher)

  // 버튼 클릭시에 무슨 작업을 할 것인가!
  var listener = object : DialogInterface.OnClickListener {
   override fun onClick(p0: DialogInterface?, p1: Int){
    when (p1) {
     DialogInterface.BUTTON_POSITIVE -> {
      delete.deleteItem(context,displaylist,pos)
      notifyItemRemoved(pos)
      notifyItemRangeChanged(pos, displaylist.size)
      Toast.makeText(context.applicationContext, displaylist[pos].name + "가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
     }

     DialogInterface.BUTTON_NEGATIVE ->{
      Toast.makeText(context.applicationContext,"삭제가 취소되었습니다.", Toast.LENGTH_SHORT).show()
     }
    }
   }
  }
  builder.setPositiveButton("확인", listener)
  builder.setNegativeButton("취소", listener)
  builder.show()

 }

 fun editItem(pos: Int){
  val intent = Intent(context, EditActivity::class.java)
  intent.putExtra("item", displaylist[pos] as Serializable)
  context.startActivity(intent)
 }
}

