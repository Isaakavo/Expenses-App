package adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.monthlyexpenses.R
import model.Expenses
import java.text.DateFormat

//Recycler view adapter to show the list of expenses or display a menu on a selected expense to
//Delete or edit
class ExpenseListAdapter(private val onEditSelected: OnEditSelectedListener,
                         private val onClickListener: OnClickListener) :
    ListAdapter<Expenses, RecyclerView.ViewHolder>(ExpenseComparator()) {

    lateinit var expense: Expenses

    //Interfaces to know which expense has been selected for edit or delete
    interface OnEditSelectedListener {
        fun sendExpenseToEdit(expense: Expenses)
        fun sendExpenseToDelete(expense: Expenses)
    }

    //Interface to display the items of the desired expense
    interface OnClickListener {
        fun onExpenseItemClicked(expense: Expenses)
    }

    private val SHOW_MENU = 1
    private val HIDE_MENU = 2

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
            MenuViewHolder.create(parent)
        }else{
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)
            return ExpenseViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        expense = getItem(position)
        if (holder is ExpenseViewHolder) {
            //When two or more expenses has the same date, the date text view is hide to only show one
            //date text view
            when {
                position != 0 -> {
                    val previousExpense: Expenses = getItem(position - 1)
                    val currentExpenseDate = holder.setDateFormat(expense.date)
                    val previousExpenseDate = holder.setDateFormat(previousExpense.date)
                    holder.bind(expense)
                    holder.setSectionDate(currentExpenseDate, previousExpenseDate)
                }
                position == 0 -> {
                    holder.bindPositionZero(expense)
                }
            }
        }else if (holder is MenuViewHolder){
            holder.buttonEdit?.setOnClickListener{
                holder.editItem(getItem(position), onEditSelected)
            }
            holder.deleteButton?.setOnClickListener{
                holder.deleteItem(getItem(position), onEditSelected)
            }
        }
    }

    inner class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvExpense: TextView = view.findViewById(R.id.tvExpenseTitle)
        private val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        private val tvSectionDate: TextView = view.findViewById(R.id.sectionDate)

        private lateinit var expenses: Expenses

        //Init the interface to display data of expense
        init {
            view.setOnClickListener {
                onClickListener.onExpenseItemClicked(expenses)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(expense: Expenses) {
            tvExpense.text = expense.concept
            tvTotal.text = "$" + expense.total.toString()
            expenses = expense
        }
        fun bindPositionZero(expense: Expenses){
            tvExpense.text = expense.concept
            tvTotal.text = "$" + expense.total.toString()
            tvSectionDate.text = setDateFormat(expense.date)
            tvSectionDate.visibility = View.VISIBLE
            expenses = expense
        }

        //Function that set show or hide the date text view
        fun setSectionDate(actualExpenseDate: String, previousExpenseDate: String) {
            if (actualExpenseDate == previousExpenseDate) {
                tvSectionDate.visibility = View.GONE
            } else {
                tvSectionDate.text = setDateFormat(expense.date)
                tvSectionDate.visibility = View.VISIBLE
            }
        }

        fun setDateFormat(timestamp: Long): String{
            return DateFormat.getDateInstance().format(timestamp)
        }
    }

    //Menu view holder
    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val buttonEdit: LinearLayout? = view.findViewById(R.id.editButton)
        val deleteButton: LinearLayout? = view.findViewById(R.id.deleteButton)

        fun editItem(expense: Expenses, listener: OnEditSelectedListener) {
            listener.sendExpenseToEdit(expense)
        }

        fun deleteItem(expense: Expenses, listener: OnEditSelectedListener) {
            listener.sendExpenseToDelete(expense)
        }

        companion object {
            fun create(parent: ViewGroup): MenuViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_menu, parent, false)
                return MenuViewHolder(view)
            }
        }
    }

    class ExpenseComparator: DiffUtil.ItemCallback<Expenses>() {
        override fun areItemsTheSame(oldItem: Expenses, newItem: Expenses): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expenses, newItem: Expenses): Boolean {
            return oldItem == newItem
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