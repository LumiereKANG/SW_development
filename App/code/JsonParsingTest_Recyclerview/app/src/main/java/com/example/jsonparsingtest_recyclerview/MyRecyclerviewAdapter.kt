package com.example.jsonparsingtest_recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jsonparsingtest_recyclerview.databinding.ListItemBinding

class MyRecyclerviewAdapter(private val dataset: FoodExpirationDate) : RecyclerView.Adapter<MyRecyclerviewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

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
