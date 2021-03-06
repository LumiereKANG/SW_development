package com.example.receipt.edit

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.receipt.MainActivity
import com.example.receipt.R
import com.example.receipt.room.Mylist
import com.example.receipt.room.MylistDB
import kotlinx.android.synthetic.main.activity_edit.*
import org.json.JSONArray
import java.util.Calendar;
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        var item = intent.getSerializableExtra("item") as Mylist


        iv_editimage.setImageResource(item.item)
        tv_edititem.setText(item.name)
        tv_editstate.setText("보관상태 수정")

        tv_dateTitle.setText("유통기한 마지막 날 수정")
        tv_editdateExplan.setText("소비기한은 유통기한 마지막날을 기준으로 계산됩니다.")

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-d")
        var startDate = LocalDate.parse(item.dates, formatter).minusDays(jsonParsingDay(item).toLong()).toString()
        tv_editDate.setText(startDate)
        tv_editResultTitle.setText("수정된 소비기한")
        tv_editResult.setText(item.dates)


        radioGroup.setOnCheckedChangeListener{ group, checkedId->
            when(checkedId){
                R.id.refri_btn -> {
                    item.dates = LocalDate.parse(startDate, formatter).plusDays(jsonParsingDay(item).toLong()).toString()
                    tv_editResult.setText(item.dates)

                    cal_btn.setOnClickListener{
                        var daySelect = ""
                        val cal = Calendar.getInstance()    //캘린더뷰 만들기
                        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                            daySelect = "${year}-${month+1}-${dayOfMonth}"
                            val dayInfo = jsonParsingDay(item)
                            item.dates = calculateDate(daySelect,dayInfo)
                            tv_editDate.setText(daySelect)
                            tv_editResult.setText(item.dates)
                        }
                        DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                }

                R.id.roomtem_btn ->{
                    item.dates = LocalDate.parse(startDate, formatter).plusDays(3.toLong()).toString()
                    tv_editResult.setText(item.dates)

                    cal_btn.setOnClickListener{
                        var daySelect = ""
                        val cal = Calendar.getInstance()    //캘린더뷰 만들기
                        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                            daySelect = "${year}-${month+1}-${dayOfMonth}"
                            val dayInfo = "3"
                            item.dates = calculateDate(daySelect,dayInfo)
                            tv_editDate.setText(daySelect)
                            tv_editResult.setText(item.dates)
                        }
                        DatePickerDialog(this, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                }
            }
        }

        edit_ok.setOnClickListener{
            Log.d("데이터 베이스 저장",item.dates)
            val r = Runnable {
                MylistDB?.getInstance(this)?.mylistDao()?.update(item)!!
            }
            val thread = Thread(r)
            thread.start()

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        edit_cancle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun jsonParsingDay(item : Mylist):String{
        var jsonString = assets.open("json/data.json").reader().readText()
        val jsonArray = JSONArray(jsonString)
        val i = item.index
        val jsonObject = jsonArray.getJSONObject(i)
        val dayInfo = jsonObject.getString("ExpirationDate")

        return dayInfo
    }

    fun calculateDate(daySelect : String , dayInfo : String) : String{
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-d")
        val caldates = LocalDate.parse(daySelect, formatter).plusDays(dayInfo.toLong()).toString()
        return caldates
    }

}