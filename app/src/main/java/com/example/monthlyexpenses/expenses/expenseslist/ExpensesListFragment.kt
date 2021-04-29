package com.example.monthlyexpenses.expenses.expenseslist

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monthlyexpenses.ExpensesApplication
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.adapter.DeleteListItemListener
import com.example.monthlyexpenses.adapter.EditListItemListener
import com.example.monthlyexpenses.adapter.ExpenseListAdapter
import com.example.monthlyexpenses.adapter.ExpenseListListener
import com.example.monthlyexpenses.data.expenses.Expenses
import com.example.monthlyexpenses.databinding.FragmentExpenseListBinding
import com.example.monthlyexpenses.getSpinnerMonths
import com.example.monthlyexpenses.ui.SwipeExpense
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class ExpensesListFragment : Fragment(), AdapterView.OnItemSelectedListener {

  private lateinit var binding: FragmentExpenseListBinding
  private lateinit var recyclerAdapter: ExpenseListAdapter
  private lateinit var vibrator: Vibrator
  private lateinit var expenseListViewModel: ExpensesListViewModel

  companion object {
    const val flag = "FLAG"
    const val newExpenseActivityRequestCode = 1
    const val editExpenseActivityRequestCode = 2
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val application = requireNotNull(this.activity).application

    val viewModelFactory =
      ExpenseListViewModelFactory((application as ExpensesApplication).expensesRepository)

    expenseListViewModel =
      ViewModelProvider(this, viewModelFactory).get(ExpensesListViewModel::class.java)

    binding = DataBindingUtil.inflate(
      inflater,
      R.layout.fragment_expense_list,
      container, false
    )
    binding.expenseViewModel = expenseListViewModel
    binding.lifecycleOwner = viewLifecycleOwner
    bindViews()
    bindRecyclerView()
    expenseListViewModel.getExpenses.observe(viewLifecycleOwner, getExpensesObserver)
    expenseListViewModel.navigateToExpenseDetails.observe(viewLifecycleOwner, { expenseId ->
      expenseId?.let {
        this.findNavController().navigate(
          ExpensesListFragmentDirections.actionNavigationHomeToExpensesDetails(expenseId)
        )
        expenseListViewModel.onExpenseDetailNavigated()
      }
    })
    expenseListViewModel.navigateToEditExpense.observe(viewLifecycleOwner, { expenseId ->
      expenseId?.let {
        vibrate()
        recyclerAdapter.closeMenu()
        view?.findNavController()?.navigate(
            ExpensesListFragmentDirections
                .actionNavigationHomeToAddNewExpense(editExpenseActivityRequestCode, expenseId, null)
        )
        expenseListViewModel.onEditNavigated()
      }

    })
    expenseListViewModel.navigateToAddExpense.observe(viewLifecycleOwner, { navigate ->
      if (navigate) {
        vibrate()
        val desiredDate = expenseListViewModel.desiredDate.value
        view?.findNavController()?.navigate(
            ExpensesListFragmentDirections.actionNavigationHomeToAddNewExpense(
                newExpenseActivityRequestCode, 0, desiredDate
            )
        )
        expenseListViewModel.onAddExpenseNavigated()
      }
    })
    expenseListViewModel.showDeleteSnackBar.observe(viewLifecycleOwner, { expenseToDelete ->
      expenseToDelete?.let {
        Snackbar.make(
            requireView(),
            getString(R.string.You_have_deleted, expenseToDelete.concept),
            Snackbar.LENGTH_LONG
        )
            .setBackgroundTint(requireContext().getColor(R.color.red)).show()
        expenseListViewModel.onDeleted()
      }
    })
    expenseListViewModel.openMonthTotals.observe(viewLifecycleOwner, { open ->
      if (open == true) {
        val desiredDate = expenseListViewModel.desiredDate.value
        view?.findNavController()?.navigate(
            ExpensesListFragmentDirections
                .actionNavigationHomeToHalfMonthTotals(desiredDate!!)
        )
        expenseListViewModel.onOpenedMonthTotals()
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
        getSpinnerMonths(this.context)
      )
    arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
    binding.monthSpinner.adapter = arrayAdapter
    binding.monthSpinner.onItemSelectedListener = this
  }

  private fun bindRecyclerView() {
    recyclerAdapter = ExpenseListAdapter(
      EditListItemListener { expenseId -> editItemListener(expenseId) },
      DeleteListItemListener { expense -> deleteItemListener(expense) },
      ExpenseListListener { expenseId -> expenseListListener(expenseId) })
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

  private fun vibrate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(150, 1))
    }
  }

  private fun expenseListListener(expenseId: Long) =
    expenseListViewModel.onExpenseSelected(expenseId)

  private fun editItemListener(expenseId: Long) = expenseListViewModel.onEditSelected(expenseId)
  private fun deleteItemListener(expense: Expenses) = expenseListViewModel.onDeleteSelected(expense)

  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    val selectedItem = parent?.getItemAtPosition(position) as String
    if (selectedItem == context?.getString(R.string.All_Expenses)) {
      expenseListViewModel.setDesiredDate(requireContext().getString(R.string.All_Expenses))
    } else {
      val selectedItemFormatter =
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(selectedItem)!!
      val desiredDate = SimpleDateFormat("y-MM", Locale.getDefault()).format(selectedItemFormatter)
      expenseListViewModel.setDesiredDate(desiredDate)
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
    return
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.expenses_menu, menu)
  }

  @SuppressLint("InlinedApi")
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.add_budget -> {
        vibrate()
        val desiredDate = expenseListViewModel.desiredDate.value
        if (desiredDate != getString(R.string.All_Expenses)) {
          view?.findNavController()?.navigate(
            ExpensesListFragmentDirections
              .actionNavigationHomeToAddBudgetDialogFragment(desiredDate!!)
          )
        } else {
          Snackbar.make(
            requireView(),
            getString(R.string.You_cant_add_a_budget_when_seeing_all_expenses),
            Snackbar.LENGTH_LONG
          )
            .show()
        }
      }
    }
    return super.onOptionsItemSelected(item)
  }
}