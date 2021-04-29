package com.example.monthlyexpenses.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monthlyexpenses.data.expenses.Items
import com.example.monthlyexpenses.databinding.ListItemEditTextBinding

//Adapter to add and remove edit text from AddNewExpense activity using data binding
class EditTextAdapter : ListAdapter<Items, RecyclerView.ViewHolder>(ItemsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EditTextViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is EditTextViewHolder -> holder.bind(item)
        }
    }

    class EditTextViewHolder(private val binding: ListItemEditTextBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Items) {
            binding.item = item
        }

        companion object {
            fun from(parent: ViewGroup): EditTextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemEditTextBinding.inflate(layoutInflater, parent, false)
                return EditTextViewHolder(binding)
            }
        }
    }
}

class ItemsComparator : DiffUtil.ItemCallback<Items>() {
    override fun areItemsTheSame(oldItem: Items, newItem: Items): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Items, newItem: Items): Boolean {
        return oldItem == newItem
    }

}