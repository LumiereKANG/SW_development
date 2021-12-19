package com.example.receipt

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.widget.Toast
import com.example.receipt.possetionList.MyListDb
import com.example.receipt.recycle.MyList


class Delete {
    fun deleteItem(context: Context, displaylist: ArrayList<MyList>, pos: Int) {
        val rt = Runnable {
            MyListDb.getInstance(context)?.myListDao()?.delete(displaylist[pos])!!
            if (displaylist != null) {
                Log.d("진짜삭제된 품목명", displaylist[pos].name)
                displaylist.removeAt(pos)
                Log.d("삭제사이즈", displaylist.size.toString())
                Log.d("진짜삭제성공", pos.toString())
            } else {
            }
        }
        val thread = Thread(rt)
        thread.start()
    }
}