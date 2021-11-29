package com.example.receipt.edit

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.receipt.MainActivity
import com.example.receipt.R
import com.example.receipt.room.Mylist
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item.*
import java.util.*
import java.util.Calendar;
import org.stringtemplate.v4.compiler.STLexer.str
import java.text.SimpleDateFormat
import org.stringtemplate.v4.compiler.STLexer.str





class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        var item = intent.getSerializableExtra("item") as Mylist

        var viewDate = item.dates.toString()

        iv_editimage.setImageResource(item.item)
        tv_edititem.setText(item.name)
        tv_editstate.setText("보관상태 수정")

        tv_dateTitle.setText("유통기한 마지막 날 수정")
        tv_editdateExplan.setText("소비기한은 유통기한 마지막날을 기준으로 계산됩니다.")
        tv_edit_Date.setText(viewDate)

        edit_cancle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        edit_ok.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }


        cal_btn.setOnClickListener{
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                viewDate = "${year}-${month+1}-${dayOfMonth}"
                val format = SimpleDateFormat("yyyy-MM-dd")
                val calDate = format.parse(viewDate)
                tv_edit_Date.setText(viewDate)
            }
            DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

}