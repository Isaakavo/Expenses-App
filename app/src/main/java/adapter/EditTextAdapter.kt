package adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monthlyexpenses.databinding.EditTextItemBinding
import model.Items

//Adapter to add and remove edit text from AddNewExpense activity using data binding
class EditTextAdapter(private val editTextList: ArrayList<Items>) :
    RecyclerView.Adapter<EditTextAdapter.EditTextViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditTextViewHolder =
        EditTextViewHolder(
            EditTextItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: EditTextViewHolder, position: Int) {
        holder.bind(editTextList[position])
    }

    override fun getItemCount(): Int = editTextList.size

    inner class EditTextViewHolder(private val binding: EditTextItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Items) {
            binding.item = item
            binding.root.requestFocus()
        }
    }
}