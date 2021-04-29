package com.example.monthlyexpenses.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monthlyexpenses.data.expenses.Expenses
import com.example.monthlyexpenses.databinding.ListItemExpenseListBinding
import com.example.monthlyexpenses.databinding.ListItemMenuBinding
import java.text.DateFormat


private const val SHOW_MENU = 1
private const val HIDE_MENU = 2

//Recycler view adapter to show the list of expenses or display a menu on a selected expense to
//Delete or edit
class ExpenseListAdapter(
    private val editClickListener: EditListItemListener,
    private val deleteClickListener: DeleteListItemListener,
    private val clicklistener: ExpenseListListener
) :
    ListAdapter<Expenses, RecyclerView.ViewHolder>(ExpenseComparator()) {

    //Interfaces to know which expense has been selected for edit or delete
    interface OnEditSelectedListener {
        fun sendExpenseToEdit(expense: Expenses)
        fun sendExpenseToDelete(expense: Expenses)
    }

    override fun getItemViewType(position: Int): Int {
        val expenseItem = getItem(position)
        return if (expenseItem.showMenu) {
            SHOW_MENU
        } else {
            HIDE_MENU
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SHOW_MENU){
            MenuViewHolder.from(parent)
        }else{
            ExpenseViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val expense = getItem(position)
        if (holder is ExpenseViewHolder) {
//            //When two or more expenses has the same date, the date text view is hide to only show one
//            //date text view
            when {
                position > 0 -> {
                    val previousExpense = getItem(position - 1)
                    holder.bind(expense, previousExpense, clicklistener)
                }
                else -> {
                    holder.bindFirstItem(expense, clicklistener)
                }
            }
        }else if (holder is MenuViewHolder){
            holder.bind(expense, editClickListener, deleteClickListener)
        }
    }

    class ExpenseViewHolder(val binding: ListItemExpenseListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expenses, previousExpense: Expenses, clicklistener: ExpenseListListener) {
            binding.id = expense.id
            binding.concept = expense.concept
            binding.date = expense.date
            binding.total = expense.total
            binding.executePendingBindings()
            binding.clicklistener = clicklistener
            setSectionDate(expense.date, previousExpense.date)
        }

        fun bindFirstItem(expense: Expenses, clicklistener: ExpenseListListener) {
            binding.id = expense.id
            binding.concept = expense.concept
            binding.date = expense.date
            binding.total = expense.total
            binding.clicklistener = clicklistener
            binding.executePendingBindings()
        }

        //Function that set show or hide the date text view
        private fun setSectionDate(actualExpenseDate: Long, previousExpenseDate: Long) {
            val actualExpenseDateFormat = setDateFormat(actualExpenseDate)
            val previousExpenseDatFormat = setDateFormat(previousExpenseDate)
            if (actualExpenseDateFormat == previousExpenseDatFormat) {
                binding.sectionDate.visibility = View.GONE
            } else {
                binding.sectionDate.visibility = View.VISIBLE
            }
        }

        private fun setDateFormat(timestamp: Long): String {
            return DateFormat.getDateInstance().format(timestamp)
        }

        companion object {
            fun from(parent: ViewGroup): ExpenseViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemExpenseListBinding.inflate(layoutInflater, parent, false)
                return ExpenseViewHolder(binding)
            }
        }
    }

    //Menu view holder
    class MenuViewHolder(val binding: ListItemMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            expense: Expenses,
            editClickListener: EditListItemListener,
            deleteClickListener: DeleteListItemListener
        ) {
            binding.expense = expense
            binding.editClickListener = editClickListener
            binding.deleteClickListener = deleteClickListener
        }

        companion object {
            fun from(parent: ViewGroup): MenuViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemMenuBinding.inflate(layoutInflater, parent, false)
                return MenuViewHolder(binding)
            }
        }
    }

    fun showMenu(position: Int){
        val expenseList = currentList
        for (i in 0 until expenseList.size){
            expenseList[i].showMenu = false
        }
        expenseList[position].showMenu = true
        notifyDataSetChanged()
    }

    fun closeMenu() {
        val expenseList = currentList
        for (i in 0 until expenseList.size) {
            expenseList[i].showMenu = false
        }
        notifyDataSetChanged()
    }

    fun isMenuShown(): Boolean {
        val expenseList = currentList
        for (i in 0 until expenseList.size) {
            if (expenseList[i].showMenu) {
                return true
            }
        }
        return false
    }
}

class ExpenseComparator : DiffUtil.ItemCallback<Expenses>() {
    override fun areItemsTheSame(oldItem: Expenses, newItem: Expenses): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Expenses, newItem: Expenses): Boolean {
        return oldItem == newItem
    }
}

class ExpenseListListener(val clicklistener: (id: Long) -> Unit) {
    fun onClick(id: Long) = clicklistener(id)
}

class EditListItemListener(val clicklistener: (expenseId: Long) -> Unit) {
    fun onEditClick(expense: Expenses) = clicklistener(expense.id)
}

class DeleteListItemListener(val clicklistener: (expense: Expenses) -> Unit) {
    fun onDeleteClick(expense: Expenses) = clicklistener(expense)
}