package com.example.monthlyexpenses.expenses

import UI.DatePickerFragment
import adapter.EditTextAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.ActivityAddNewExpenseBinding
import com.google.android.material.snackbar.Snackbar
import model.Expenses
import model.Items
import viewmodel.ExpenseViewModel
import viewmodel.ExpenseViewModelFactory
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddNewExpense : AppCompatActivity(), View.OnClickListener {
  //UI components
  private lateinit var editTextConcept: EditText
  private lateinit var editTextDate: EditText
  private lateinit var buttonAdd: Button
  private lateinit var addNewComment: ImageView
  private lateinit var removeNewComent: ImageView
  private lateinit var recyclerView: RecyclerView

  private var flag = 0
  private var timestamp: Long = 0

  //Variable to get the id of the expense for edit
  private var idExpense = 0L

  private val context: Context = this

  private val itemList: ArrayList<Items> = arrayListOf()
  private val itemListToDelete: ArrayList<Items> = arrayListOf()
  private lateinit var editTextAdapter: EditTextAdapter
  private lateinit var binding: ActivityAddNewExpenseBinding


  private val expenseViewModel: ExpenseViewModel by viewModels {
    ExpenseViewModelFactory((application as ExpensesApplication).repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_add_new_expense)
    bindViews()
    setAdapter()
    setListener()

    //Get the flag to know if user want to add or update an expense.
    //If user want to edit we populate the edit text for expense data and create dynamically edit text for items
    flag = intent.getIntExtra(ExpensesListFragment.flag, 0)
    if (flag == ExpensesListFragment.editExpenseActivityRequestCode) {
      val expenseToEdit = intent.getSerializableExtra(EXTRA_EXPENSE) as Expenses
      timestamp = expenseToEdit.date
      idExpense = expenseToEdit.id
      editTextConcept.setText(expenseToEdit.concept)
      editTextDate.setText(DateFormat.getDateInstance().format(expenseToEdit.date))
      expenseViewModel.getItemById(expenseToEdit.id).observe(this, { items ->
        items?.let {
          for (item in it) {
            itemList.add(item)
            editTextAdapter.notifyItemInserted(itemList.size - 1)
          }
        }
      })
      buttonAdd.text = getString(R.string.update_button)
    } else if (flag == ExpensesListFragment.newExpenseActivityRequestCode) {
      itemList.add(Items())
      editTextAdapter.notifyItemInserted(itemList.size - 1)
      editTextDate.setText(setFormattedDate())
    }
  }
  private fun bindViews() {
    supportActionBar?.apply {
      title = getString(R.string.add_new_expense)
      setDisplayHomeAsUpEnabled(true)
      setDisplayShowHomeEnabled(true)
    }

    editTextConcept = binding.etConcept
    editTextDate = binding.etDate
    buttonAdd = binding.okbutton
    addNewComment = binding.addNewComment
    removeNewComent = binding.removeNewComment
  }

  //Set the adapter for data binding recycler view
  private fun setAdapter() {
    editTextAdapter = EditTextAdapter(itemList)
    recyclerView = binding.recyclerviewAddExpense.apply {
      layoutManager = LinearLayoutManager(this@AddNewExpense)
      adapter = editTextAdapter
    }
  }

  //set the listener for click functions
  private fun setListener() {
    addNewComment.setOnClickListener(this)
    removeNewComent.setOnClickListener(this)
    buttonAdd.setOnClickListener(this)
    editTextDate.setOnClickListener(this)
  }

  //Send the data to be added to data base in main viewmodel
  private fun sendExpenseToAdd() {
    val replyIntent = Intent()
    if (editTextIsNotEmpty()) {
      //val editTextConcept = binding.etConcept.text.toString()
      val expense = Expenses(editTextConcept.text.toString(), timestamp, "expense", getTotals())
      expense.id = idExpense
      replyIntent.putExtra(EXTRA_EXPENSE, expense)
      replyIntent.putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(itemList))
      replyIntent.putParcelableArrayListExtra(EXTRA_ITEMS_DELETE, ArrayList(itemListToDelete))

      when (intent.getIntExtra(ExpensesListFragment.flag, 0)) {
        ExpensesListFragment.newExpenseActivityRequestCode -> replyIntent.putExtra(
            ExpensesListFragment.returnFlag,
            ExpensesListFragment.newExpenseActivityRequestCode
        )
        ExpensesListFragment.editExpenseActivityRequestCode -> replyIntent.putExtra(
            ExpensesListFragment.returnFlag,
            ExpensesListFragment.editExpenseActivityRequestCode
        )
      }
      setResult(Activity.RESULT_OK, replyIntent)
      finish()
    }
  }

  private fun editTextIsNotEmpty(): Boolean {
    return if (editTextConcept.text.isNotEmpty() && editTextDate.text.isNotEmpty()) {
      true
    } else {
      closeKeyboard()
      if (editTextConcept.text.isEmpty()) editTextConcept.backgroundTintList =
        context.getColorStateList(R.color.red)
      if (editTextDate.text.isEmpty()) editTextDate.backgroundTintList =
        context.getColorStateList(R.color.red)
      itemList.forEach {
        if (it.item.isEmpty() or it.price.isEmpty()) Snackbar.make(
            window.decorView.rootView,
            getString(R.string.emptyEditText),
            Snackbar.LENGTH_LONG
        ).show()
      }
      Snackbar.make(window.decorView.rootView, getString(R.string.all_field_must_be_filled), Snackbar.LENGTH_LONG)
        .show()
      false
    }
  }

  private fun setFormattedDate(): String {
    val calendar = Calendar.getInstance()
    timestamp = calendar.timeInMillis
    return DateFormat.getDateInstance().format(timestamp)
  }

  private fun getTotals(): Float {
    var totalItems = 0F
    itemList.forEach { items ->
      totalItems += items.price.toFloat()
    }
    return totalItems
  }

  private fun showDatePickerDialog() {
    val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
    datePicker.show(supportFragmentManager, "datePicker")
  }

  private fun onDateSelected(day: Int, month: Int, year: Int) {
    val combinedCal = GregorianCalendar(TimeZone.getTimeZone("GMT-06:00"))
    combinedCal.set(year, month, day)
    val timestamp = combinedCal.timeInMillis
    this.timestamp = timestamp
    val selectedDate = DateFormat.getDateInstance().format(timestamp)
    editTextDate.setText(selectedDate.toString())
  }

  private fun closeKeyboard() {
    val view = this.currentFocus
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
  }

  companion object {
    const val EXTRA_EXPENSE = "com.example.android.expenselistsql.EXPENSE"
    const val EXTRA_ITEMS = "com.example.android.expenselistsql.ITEM"
    const val EXTRA_ITEMS_DELETE = "com.example.android.expenselistsql.ITEMDELETE"
  }

  override fun onClick(v: View?) {
    when (v?.id) {
      addNewComment.id -> {
        itemList.add(Items())
        editTextAdapter.notifyItemInserted(itemList.size - 1)
      }
      removeNewComent.id -> {
        if (itemList.size > 1) {
          itemListToDelete.add(itemList.last())
          itemList.removeLast()
          editTextAdapter.notifyItemRemoved(itemList.size)
        }
      }
      buttonAdd.id -> {
        sendExpenseToAdd()
      }
      editTextDate.id -> {
        showDatePickerDialog()
      }
    }
  }
}