package com.example.monthlyexpenses.expenses

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentAddBudgetBinding
import viewmodel.ExpenseViewModel


class AddBudgetDialogFragment : DialogFragment() {

  private val expenseViewModel: ExpenseViewModel by activityViewModels()
  private lateinit var binding: FragmentAddBudgetBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_budget, container, false)
    binding.budgets = expenseViewModel
    binding.lifecycleOwner = viewLifecycleOwner
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.addBudget.setOnClickListener {
      expenseViewModel.insertBudget()
      dialog?.dismiss()
    }
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