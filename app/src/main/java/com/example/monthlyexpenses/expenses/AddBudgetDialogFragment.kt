package com.example.monthlyexpenses.expenses

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.monthlyexpenses.R
import model.Budget
import viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*


class AddBudgetDialogFragment : DialogFragment() {

  private val expenseViewModel: ExpenseViewModel by activityViewModels()

  lateinit var firstFortnight: EditText
  lateinit var secondFortnight: EditText
  lateinit var budgetTitle: TextView
  lateinit var addBudget: Button
  var idBudget: Long = 1
  private var currentDateInMillis: Long = 0

  private val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_add_budget, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    bindViews(view)

    expenseViewModel.getBudgets.observe(this, {
        val selectedItemFormatter =
            SimpleDateFormat("y-MM", Locale.getDefault()).parse(expenseViewModel.desiredDate.value!!)
        val formattedDate = formatter.format(selectedItemFormatter!!)
        currentDateInMillis = selectedItemFormatter.time

        budgetTitle.text = getString(R.string.add_or_edit_budget_for_this_month, formattedDate)
        if (it != null) {
            firstFortnight.setText(it.budgetForFirstFortnight.toString())
            secondFortnight.setText(it.budgetForSecondFortnight.toString())
            idBudget = it.id
            addBudget.setText(R.string.update_button)
        }
    })

    addBudget.setOnClickListener {
      if (firstFortnight.text.isEmpty()) firstFortnight.setText("0.0")
      if (secondFortnight.text.isEmpty()) secondFortnight.setText("0.0")

      val budgetToAdd = Budget(firstFortnight.text.toString().toFloat(), secondFortnight.text.toString().toFloat(), currentDateInMillis)
      budgetToAdd.id = idBudget
      expenseViewModel.insertBudget(budgetToAdd)
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


  private fun bindViews(view: View) {
    firstFortnight = view.findViewById(R.id.first_fortnight)
    secondFortnight = view.findViewById(R.id.second_fortnight)
    budgetTitle = view.findViewById(R.id.addBudgetTitle)
    addBudget = view.findViewById(R.id.addBudget)

    firstFortnight.setText("0.0")
    secondFortnight.setText("0.0")
  }
}