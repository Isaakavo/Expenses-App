package com.example.monthlyexpenses.expenses.expensesdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.monthlyexpenses.ExpensesApplication
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentExpensesDetailsBinding
import timber.log.Timber

private const val ARG_PARAM1 = "param1"

//Fragment to show the details of the expenses
class ExpensesDetails : DialogFragment() {

  private lateinit var binding: FragmentExpensesDetailsBinding
  private lateinit var expenseDetailsViewModel: ExpenseDetailsViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    val args = ExpensesDetailsArgs.fromBundle(requireArguments())

    val application = requireNotNull(this.activity).application

    val viewModelFactory =
      ExpenseDetailViewModelFactory((application as ExpensesApplication).repository, args.expenseId)

    expenseDetailsViewModel =
      ViewModelProvider(this, viewModelFactory).get(ExpenseDetailsViewModel::class.java)

    binding =
      DataBindingUtil.inflate(inflater, R.layout.fragment_expenses_details, container, false)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.expenses = expenseDetailsViewModel

    val recyclerAdapter = ExpenseDetailsAdapter()
    binding.itemsRecyclerview.apply {
      adapter = recyclerAdapter
      isNestedScrollingEnabled = false
    }
    expenseDetailsViewModel.getItems.observe(viewLifecycleOwner, { items ->
      Timber.d("$items")
      items?.let {
        recyclerAdapter.submitList(items)
      }
    })

    return binding.root
  }

  //Make the card size of the screen
  override fun onStart() {
    super.onStart()
    dialog?.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
  }

}