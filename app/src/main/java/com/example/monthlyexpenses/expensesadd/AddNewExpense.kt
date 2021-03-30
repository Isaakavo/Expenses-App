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
import timber.log.Timber

class AddNewExpense : Fragment() {

  private lateinit var editTextAdapter: EditTextAdapter
  private lateinit var binding: ActivityAddNewExpenseBinding

  private lateinit var listener: OnAddNewExpenseOpen

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
    val expenseAddViewModel =
      ViewModelProvider(this, viewModelFactory).get(ExpensesAddViewModel::class.java)

    binding = DataBindingUtil.inflate(inflater, R.layout.activity_add_new_expense, container, false)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.viewmodel = expenseAddViewModel
    setAdapter()
    //Get the flag to know if user want to add or update an expense.
    //If user want to edit we populate the edit text for expense data and create dynamically edit text for items
    if (args.flag == ExpensesListFragment.editExpenseActivityRequestCode) {
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
          Timber.d("Item list live data $it")
          editTextAdapter.submitList(items.toList())
        }

      })
      expenseAddViewModel.items.observe(viewLifecycleOwner, {
        expenseAddViewModel.setItemList(it)
      })
    } else if (args.flag == ExpensesListFragment.newExpenseActivityRequestCode) {
      expenseAddViewModel.itemListLiveData.observe(viewLifecycleOwner, { items ->
        editTextAdapter.submitList(items.toList())
      })
      expenseAddViewModel.concept.observe(viewLifecycleOwner, { concept ->
        Timber.d(concept)
        if (concept.isNullOrEmpty()) {
          binding.etConcept.backgroundTintList =
            context?.getColorStateList(R.color.red)
          Snackbar.make(requireView(), "You must set a concept!", Snackbar.LENGTH_SHORT).show()
        } else {
          expenseAddViewModel.isConceptFill = true
        }
      })
    }

    expenseAddViewModel.showDatePicker.observe(viewLifecycleOwner, {
      if (it) {
        showDatePickerDialog(expenseAddViewModel)
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

  private fun showDatePickerDialog(expensesAddViewModel: ExpensesAddViewModel) {
    val datePicker = DatePickerFragment(expensesAddViewModel)
    datePicker.show(childFragmentManager, "datePicker")
  }

  private fun closeKeyboard() {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
  }
}