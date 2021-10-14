package com.example.jsonparsingrecyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jsonparsingrecyclerview.databinding.ListItemBinding

// 1. 외부에서 FoodExpirationData를 받아옴
class MyRecyclerviewAdapter(private val dataset: FoodExpirationDate) : RecyclerView.Adapter<MyRecyclerviewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // 3. 선택한 position의 FoodExpirationData를 ViewHolder에 전달
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    // recyclerview의 크기 = 불러온 dataset의 크기
    override fun getItemCount(): Int {
        return dataset.size
    }

    // 2. 받아온 data를 Viewholer에 전달
    class ViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FoodExpirationDateItem) {
            with (binding) {
                tvName.text = data.food
                tvCategory.text = data.category
                tvExpirationdate.text = data.expirationDate.toString()
            }
        }
    }




}