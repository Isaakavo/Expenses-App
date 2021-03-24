package com.example.monthlyexpenses.expenses

import UI.MonthSpinner
import UI.SwipeExpense
import adapter.ExpenseListAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentExpenseListBinding
import com.google.android.material.snackbar.Snackbar
import model.Expenses
import model.Items
import viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

class ExpensesListFragment : Fragment(),
    ExpenseListAdapter.OnEditSelectedListener,
    ExpenseListAdapter.OnClickListener,
    View.OnClickListener {

  private val expenseViewModel: ExpenseViewModel by activityViewModels()

  private lateinit var binding: FragmentExpenseListBinding

  private lateinit var recyclerAdapter: ExpenseListAdapter

  private lateinit var vibrator: Vibrator

  private var totalFirstHalf = 0F
  private var totalSecondHalf = 0F
  private var budgetFirstHalf = 0F
  private var budgetSecondHalf = 0F

  companion object {
    const val flag = "FLAG"
    const val returnFlag = "RETURN_FLAG"
    const val newExpenseActivityRequestCode = 1
    const val editExpenseActivityRequestCode = 2
  }

  //From here we can decide what to do. If we want to add new expense to DB or edit one existing expense
  private val startAddNewExpenseForResult =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val flag = result.data?.getIntExtra(returnFlag, 0)
        if (result.resultCode == Activity.RESULT_OK && flag == newExpenseActivityRequestCode) {
          //Get the data from the activity
          val intentExpenses =
              result.data?.extras?.getSerializable(AddNewExpense.EXTRA_EXPENSE) as Expenses
          val itemList =
              result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS)
          //Insert data to data base
          expenseViewModel.insert(intentExpenses, itemList as List<Items>)
        } else if (result.resultCode == Activity.RESULT_OK && flag == editExpenseActivityRequestCode) {
          val intentExpenses =
              result.data?.extras?.getSerializable(AddNewExpense.EXTRA_EXPENSE) as Expenses
          val itemList =
              result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS)
          //If the user want to delete an item we recover wich data and the we delete it using
          //delete items method from view model
          val itemsToDelete =
              result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS_DELETE)
          expenseViewModel.updateExpenseAndItems(intentExpenses, itemList as List<Items>)
          expenseViewModel.deleteItems(itemsToDelete as ArrayList)
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
          Snackbar.make(requireView(), "Something went wrong!", Snackbar.LENGTH_LONG).show()
        }
      }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {

    vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    binding = DataBindingUtil.inflate(
        inflater,
        R.layout.fragment_expense_list,
        container, false)
    binding.expenseViewModel = expenseViewModel
    binding.lifecycleOwner = viewLifecycleOwner
    bindViews()
    bindRecyclerView()
    expenseViewModel.getExpenses.observe(viewLifecycleOwner, getExpensesObserver)
    //expenseViewModel.getBudgets.observe(viewLifecycleOwner, budgetObserver)
    expenseViewModel.eventOpTvMonthTotal.observe(viewLifecycleOwner, { isOpen ->
      if (isOpen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createOneShot(150, 1))
        }
        val halfMonthTotals = HalfMonthTotals()
        halfMonthTotals.show(childFragmentManager, "Totals")
        expenseViewModel.onOpenMonthTotalFragmentComplete()
      }
    })
    return binding.root
  }

  @SuppressLint("InlinedApi")
  private fun bindViews() {
    val arrayAdapter =
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            getSpinnerMonths()
        )
    arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
    binding.monthSpinner.adapter = arrayAdapter
    binding.monthSpinner.onItemSelectedListener = MonthSpinner(expenseViewModel)
    //When user clicks on total of month a fragment is displayed to show the expenses by half month
    binding.TVIncome.setOnClickListener(this)
    binding.fabMenu.setOnClickListener(this)
    binding.fabMenu.setOnLongClickListener {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.EFFECT_DOUBLE_CLICK))
      }
      val addBudgetFragment = AddBudgetDialogFragment()
      addBudgetFragment.show(childFragmentManager, "AddBudget")
      true
    }

  }

  private fun bindRecyclerView() {
    recyclerAdapter = ExpenseListAdapter(this, this)
    binding.recyclerview.apply {
      adapter = recyclerAdapter
      layoutManager = LinearLayoutManager(activity?.applicationContext)
      isNestedScrollingEnabled = false
    }
    //Set the adapter to swipe function
    val itemTouchHelper = ItemTouchHelper(SwipeExpense(context, recyclerAdapter))
    itemTouchHelper.attachToRecyclerView(binding.recyclerview)
  }
  private val getExpensesObserver = Observer<List<Expenses>> { expenses ->
    expenses?.let {
      recyclerAdapter.submitList(it)
    }
  }
  override fun onClick(v: View?) {
    when (v?.id) {
      binding.budgetForMonth.id -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createOneShot(150, 1))
        }
//        val halfMonthTotals =
//            HalfMonthTotals.newInstance(budgetFirstHalf.toString(), budgetSecondHalf.toString(), getString(R.string.budgets_of_the_month))
//        halfMonthTotals.show(childFragmentManager, "Budgets")
      }
      binding.fabMenu.id -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createOneShot(150, 1))
        }
        val intent = Intent(activity, AddNewExpense::class.java)
        intent.putExtra(flag, newExpenseActivityRequestCode)
        recyclerAdapter.closeMenu()
        startAddNewExpenseForResult.launch(intent)
      }
    }
  }

  //Interface from ExpenseListAdapter to start edit activity with data to edit
  override fun sendExpenseToEdit(expense: Expenses) {
    val intent = Intent(activity, AddNewExpense::class.java)
    intent.putExtra(flag, editExpenseActivityRequestCode)
    intent.putExtra(AddNewExpense.EXTRA_EXPENSE, expense)
    startAddNewExpenseForResult.launch(intent)
  }

  //Interface from ExpenseListAdapter to delete an expense
  override fun sendExpenseToDelete(expense: Expenses) {
    expenseViewModel.deleteExpense(expense)
  }

  //Interface from ExpenseListAdapter to open the expense dialog details
  override fun onExpenseItemClicked(expense: Expenses) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(150, 1))
    }
    if (recyclerAdapter.isMenuShown()) {
      recyclerAdapter.closeMenu()
    }
    val detailsFragment =
        ExpensesDetails.newInstance(expense)
    detailsFragment.show(parentFragmentManager, "Details")
  }

  //Get the past month of the year
  private fun getSpinnerMonths(): ArrayList<String> {
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val list = arrayListOf<String>()
    Calendar.getInstance().let { calendar ->
      val monthBefore = Calendar.getInstance()
      val currentYear = calendar.get(Calendar.YEAR)
      //We loop to know which month has already pass
      for (i in 1 until 12) {
        //We only want the month of current year
        val pastYear = monthBefore.get(Calendar.YEAR)
        if (pastYear == currentYear) {
          //Format the month and add to array
          list.add(formatter.format(calendar.timeInMillis))
          //Go backwards to know which month has passed
          calendar.add(Calendar.MONTH, -1)
          monthBefore.add(Calendar.MONTH, -1)
        }
      }
    }
    list.add("All Expenses")
    return list
  }
}