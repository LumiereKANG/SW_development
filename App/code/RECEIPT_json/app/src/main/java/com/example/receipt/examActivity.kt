package com.example.receipt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.receipt.databinding.ActivityExamBinding

class examActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding by lazy { ActivityExamBinding.inflate(layoutInflater) }

        setContentView(R.layout.activity_exam)
        val bitmap = intent.getSerializableExtra("bitmap") as Data
        binding.imageView.setImageBitmap(bitmap.bit)
    }
}