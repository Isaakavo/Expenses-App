package com.example.monthlyexpenses.expenses

import UI.MonthSpinner
import UI.SwipeExpense
import adapter.ExpenseListAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentExpenseListBinding
import model.Expenses
import viewmodel.ExpenseViewModel

class ExpensesListFragment : Fragment(),
    ExpenseListAdapter.OnEditSelectedListener,
    ExpenseListAdapter.OnClickListener,
    View.OnClickListener {

  private val expenseViewModel: ExpenseViewModel by activityViewModels()
  private lateinit var binding: FragmentExpenseListBinding
  private lateinit var recyclerAdapter: ExpenseListAdapter
  private lateinit var vibrator: Vibrator

  companion object {
    const val flag = "FLAG"
    const val newExpenseActivityRequestCode = 1
    const val editExpenseActivityRequestCode = 2
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
          MonthSpinner.getSpinnerMonths()
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
        view?.findNavController()?.navigate(
          ExpensesListFragmentDirections
            .actionNavigationHomeToAddNewExpense(
              newExpenseActivityRequestCode,
              Expenses("", 0L, "", 0F)
            )
        )
      }
    }
  }

  //Interface from ExpenseListAdapter to start edit activity with data to edit
  override fun sendExpenseToEdit(expense: Expenses) {
    view?.findNavController()?.navigate(
      ExpensesListFragmentDirections
        .actionNavigationHomeToAddNewExpense(editExpenseActivityRequestCode, expense)
    )
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
}