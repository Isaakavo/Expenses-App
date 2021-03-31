package com.example.monthlyexpenses.expensesadd

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monthlyexpenses.ExpensesApplication
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.adapter.EditTextAdapter
import com.example.monthlyexpenses.databinding.ActivityAddNewExpenseBinding
import com.example.monthlyexpenses.expenseslist.ExpensesListFragment
import com.example.monthlyexpenses.setDateFormat
import com.example.monthlyexpenses.ui.DatePickerFragment
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddNewExpense : Fragment() {

  private lateinit var editTextAdapter: EditTextAdapter
  private lateinit var binding: ActivityAddNewExpenseBinding

  private lateinit var expenseAddViewModel: ExpensesAddViewModel
  private lateinit var listener: OnAddNewExpenseOpen

  private lateinit var calendarToSend: Calendar

  /*
  * interface to communicate with main activity
  * to show and hide the bottom navvigation menu
  */
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
    val args = AddNewExpenseArgs.fromBundle(requireArguments())

    val application = requireNotNull(this.activity).application
    val viewModelFactory =
      ExpensesAddViewModelFactory((application as ExpensesApplication).repository, args.expenseId)
    expenseAddViewModel =
        ViewModelProvider(this, viewModelFactory).get(ExpensesAddViewModel::class.java)

    binding = DataBindingUtil.inflate(inflater, R.layout.activity_add_new_expense, container, false)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewmodel = expenseAddViewModel
    setAdapter()
    //Get the flag to know if user want to add or update an expense.
    //If user want to edit we populate the edit text for expense data and create dynamically edit text for items
    if (args.flag == ExpensesListFragment.editExpenseActivityRequestCode) {
      calendarToSend = Calendar.getInstance()

      binding.okbutton.text = getString(R.string.update_button)
      expenseAddViewModel.expense?.observe(viewLifecycleOwner, { expense ->
        expense?.let {
          expenseAddViewModel.concept.value = expense.concept
          expenseAddViewModel.editTextDate.value = setDateFormat(expense.date)
          expenseAddViewModel.setTimeStamp(expense.date)
          expenseAddViewModel.isConceptFill = true
        }
      })
      expenseAddViewModel.itemListLiveData.observe(viewLifecycleOwner, { items ->
        items?.let {
          editTextAdapter.submitList(items.toList())
        }

      })
      expenseAddViewModel.items.observe(viewLifecycleOwner, {
        expenseAddViewModel.setItemList(it)
      })
    } else if (args.flag == ExpensesListFragment.newExpenseActivityRequestCode) {

      calendarToSend = Calendar.getInstance()
      val dateObject =
          SimpleDateFormat("y-MM", Locale.getDefault()).parse(args.desiredDate!!)
      val calendarFormat = Calendar.getInstance()
      calendarFormat.time = dateObject!!
      val calendarNextMonth = Calendar.getInstance()
      calendarNextMonth.add(Calendar.MONTH, 1)

      if (calendarToSend.get(Calendar.DAY_OF_MONTH) >= calendarNextMonth.getMaximum(Calendar.DAY_OF_MONTH)) {
        calendarToSend.set(Calendar.DAY_OF_MONTH, calendarNextMonth.getMaximum(Calendar.DAY_OF_MONTH) - 1)
      }

      calendarToSend.set(Calendar.MONTH, calendarFormat.get(Calendar.MONTH))
      calendarToSend.set(Calendar.YEAR, calendarFormat.get(Calendar.YEAR))

      expenseAddViewModel.editTextDate.value = setDateFormat(calendarToSend.timeInMillis)
      expenseAddViewModel.setTimeStamp(calendarToSend.timeInMillis)
      expenseAddViewModel.itemListLiveData.observe(viewLifecycleOwner, { items ->
        editTextAdapter.submitList(items.toList())
      })
      expenseAddViewModel.concept.observe(viewLifecycleOwner, { concept ->
        if (concept.isNullOrEmpty()) {
          binding.etConcept.backgroundTintList =
              context?.getColorStateList(R.color.red)
          Snackbar.make(requireView(), getString(R.string.You_must_set_a_concept), Snackbar.LENGTH_SHORT).show()
        } else {
          expenseAddViewModel.isConceptFill = true
        }
      })
    }

    expenseAddViewModel.showDatePicker.observe(viewLifecycleOwner, {
      if (it) {
        showDatePickerDialog()
        expenseAddViewModel.hideDatePicker()
      }
    })

    expenseAddViewModel.navigateBackToHome.observe(viewLifecycleOwner, { navigate ->
      if (navigate) {
        view?.findNavController()?.navigate(
          AddNewExpenseDirections.actionAddNewExpenseToNavigationHome()
        )
        expenseAddViewModel.onDatabaseSent()
      }
    })
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
    editTextAdapter = EditTextAdapter()
    binding.itemListRecyclerView.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = editTextAdapter
      isNestedScrollingEnabled = false
    }
  }

  private fun showDatePickerDialog() {
    val datePicker = DatePickerFragment(calendarToSend) { day, month, year -> onDateSelected(day, month, year) }
    datePicker.show(childFragmentManager, "datePicker")
  }

  private fun onDateSelected(day: Int, month: Int, year: Int) {
    val combinedCal = GregorianCalendar(TimeZone.getTimeZone("GMT-06:00"))
    combinedCal.set(year, month, day)
    val selectedDate = DateFormat.getDateInstance().format(combinedCal.timeInMillis)
    expenseAddViewModel.editTextDate.value = selectedDate
    expenseAddViewModel.setTimeStamp(combinedCal.timeInMillis)
  }

  private fun closeKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
  }
}