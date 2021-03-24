package com.example.monthlyexpenses.expenses

import UI.DatePickerFragment
import adapter.EditTextAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.ActivityAddNewExpenseBinding
import com.google.android.material.snackbar.Snackbar
import model.Expenses
import model.Items
import viewmodel.ExpenseViewModel
import java.text.DateFormat
import java.util.*

class AddNewExpense : Fragment(), View.OnClickListener {
  private var timestamp: Long = 0

  //Variable to get the id of the expense for edit
  private var idExpense = 0L

  private val itemList: ArrayList<Items> = arrayListOf()
  private val itemListToDelete: ArrayList<Items> = arrayListOf()
  private lateinit var editTextAdapter: EditTextAdapter
  private lateinit var binding: ActivityAddNewExpenseBinding

  private lateinit var listener: OnAddNewExpenseOpen

  private val expenseViewModel: ExpenseViewModel by activityViewModels()

  interface OnAddNewExpenseOpen {
    fun onOpen()
    fun onClose()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    try {
      listener = activity as OnAddNewExpenseOpen
    } catch (e: ClassCastException) {
      throw ClassCastException(activity.toString() + "must implement" + OnAddNewExpenseOpen::class.java.simpleName)
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = DataBindingUtil.inflate(inflater, R.layout.activity_add_new_expense, container, false)
    setAdapter()
    setListener()

    val args = AddNewExpenseArgs.fromBundle(requireArguments())
    //Get the flag to know if user want to add or update an expense.
    //If user want to edit we populate the edit text for expense data and create dynamically edit text for items
    if (args.flag == ExpensesListFragment.editExpenseActivityRequestCode) {
      val expenseToEdit = args.expenseObject
      timestamp = expenseToEdit.date
      idExpense = expenseToEdit.id
      binding.etConcept.setText(expenseToEdit.concept)
      binding.etDate.setText(DateFormat.getDateInstance().format(expenseToEdit.date))
      expenseViewModel.getItemById(expenseToEdit.id).observe(viewLifecycleOwner, { items ->
        items?.let {
          for (item in it) {
            itemList.add(item)
            editTextAdapter.notifyItemInserted(itemList.size - 1)
          }
        }
      })
      binding.okbutton.text = getString(R.string.update_button)
    } else if (args.flag == ExpensesListFragment.newExpenseActivityRequestCode) {
      itemList.add(Items())
      editTextAdapter.notifyItemInserted(itemList.size - 1)
      binding.etDate.setText(setFormattedDate())
    }
    return binding.root
  }

  override fun onResume() {
    super.onResume()
    listener.onClose()
  }

  override fun onStop() {
    super.onStop()
    listener.onOpen()
  }

  //Set the adapter for data binding recycler view
  private fun setAdapter() {
    editTextAdapter = EditTextAdapter(itemList)
    binding.recyclerviewAddExpense.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = editTextAdapter
      isNestedScrollingEnabled = false
    }
  }

  //set the listener for click functions
  private fun setListener() {
    binding.addNewComment.setOnClickListener(this)
    binding.removeNewComment.setOnClickListener(this)
    binding.okbutton.setOnClickListener(this)
    binding.etDate.setOnClickListener(this)
  }

  //Send the data to be added to data base in main viewmodel
  private fun addExpenseToDatabase() {
    if (editTextIsNotEmpty()) {
      val expense = Expenses(binding.etConcept.text.toString(), timestamp, "expense", getTotals())
      expense.id = idExpense
      expenseViewModel.insert(expense, itemList)
      if (itemListToDelete.isNotEmpty()) expenseViewModel.deleteItems(itemListToDelete)
      view?.findNavController()
        ?.navigate(AddNewExpenseDirections.actionAddNewExpenseToNavigationHome())
    }
  }

  private fun editTextIsNotEmpty(): Boolean {
    return if (binding.etConcept.text.isNotEmpty() && binding.etConcept.text.isNotEmpty()) {
      true
    } else {
      closeKeyboard()
      if (binding.etConcept.text.isEmpty()) binding.etConcept.backgroundTintList =
        context?.getColorStateList(R.color.red)
      if (binding.etDate.text.isEmpty()) binding.etDate.backgroundTintList =
        context?.getColorStateList(R.color.red)
      itemList.forEach {
        if (it.item.isEmpty() or it.price.isEmpty()) Snackbar.make(
          requireView(),
          getString(R.string.emptyEditText),
          Snackbar.LENGTH_LONG
        ).show()
      }
      Snackbar.make(
        requireView(),
        getString(R.string.all_field_must_be_filled),
        Snackbar.LENGTH_LONG
      )
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
    datePicker.show(childFragmentManager, "datePicker")
  }

  private fun onDateSelected(day: Int, month: Int, year: Int) {
    val combinedCal = GregorianCalendar(TimeZone.getTimeZone("GMT-06:00"))
    combinedCal.set(year, month, day)
    val timestamp = combinedCal.timeInMillis
    this.timestamp = timestamp
    val selectedDate = DateFormat.getDateInstance().format(timestamp)
    binding.etDate.setText(selectedDate.toString())
  }

  private fun closeKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
  }
  override fun onClick(v: View?) {
    when (v?.id) {
      binding.addNewComment.id -> {
        itemList.add(Items())
        editTextAdapter.notifyItemInserted(itemList.size - 1)
      }
      binding.removeNewComment.id -> {
        if (itemList.size > 1) {
          itemListToDelete.add(itemList.last())
          itemList.removeLast()
          editTextAdapter.notifyItemRemoved(itemList.size)
        }
      }
      binding.okbutton.id -> {
        addExpenseToDatabase()
      }
      binding.etDate.id -> {
        showDatePickerDialog()
      }
    }
  }
}