package com.example.monthlyexpenses.expenses.addbudget

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.monthlyexpenses.ExpensesApplication
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentAddBudgetBinding
import com.example.monthlyexpenses.setDateMonthFormatted


class AddBudgetDialogFragment : DialogFragment() {

  private lateinit var expenseViewModel: AddBudgetViewModel
  private lateinit var binding: FragmentAddBudgetBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

    val args = AddBudgetDialogFragmentArgs.fromBundle(requireArguments())
    val application = requireNotNull(this.activity).application

    val viewModelFactory =
      AddBudgetViewModelFactory((application as ExpensesApplication).expensesRepository)

    expenseViewModel = ViewModelProvider(this, viewModelFactory).get(AddBudgetViewModel::class.java)

    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_budget, container, false)
    binding.budgets = expenseViewModel
    binding.lifecycleOwner = viewLifecycleOwner

    expenseViewModel.formattedDate.value = setDateMonthFormatted(args.desiredDate)
    expenseViewModel.dateForDB.value = args.desiredDate

    expenseViewModel.getBudgetByMonth.observe(viewLifecycleOwner, { budget ->
      budget?.let {
        expenseViewModel.firstHalfBudget.value = budget.budgetForFirstFortnight.toString()
        expenseViewModel.secondHalfBudget.value = budget.budgetForSecondFortnight.toString()
      }

      expenseViewModel.dismissDialog.observe(viewLifecycleOwner, { dismiss ->
        if (dismiss) {
          dialog?.dismiss()
          expenseViewModel.setDialogDismissed()
        }
      })

    })

    return binding.root
  }
  override fun onStart() {
    super.onStart()
    dialog?.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
    dialog?.window?.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
  }
}