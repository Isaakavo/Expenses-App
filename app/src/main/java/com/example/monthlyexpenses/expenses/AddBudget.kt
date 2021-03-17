package com.example.monthlyexpenses.expenses

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.monthlyexpenses.R
import com.google.android.material.snackbar.Snackbar
import model.Budget
import viewmodel.ExpenseViewModel
import viewmodel.ExpenseViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddBudget : AppCompatActivity() {

    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as ExpensesApplication).repository)
    }

    lateinit var firstFortnight: EditText
    lateinit var secondFortnight: EditText
    lateinit var month: EditText
    lateinit var addBudget: Button
    var idBudget: Long = 0

    private val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)

        bindViews()

        val desiredDate = intent.getStringExtra(ExpensesListFragment.desiredDateFlag)
        val selectedItemFormatter =
            SimpleDateFormat("y-MM", Locale.getDefault()).parse(desiredDate!!)
        val formattedDate = formatter.format(selectedItemFormatter!!)
        val currentDateInMillis = selectedItemFormatter.time

        month.setText(formattedDate)
        Log.d("timstamp", currentDateInMillis.toString())

        expenseViewModel.getBudgetByMonth(desiredDate).observe(this, {

            if (it != null) {
                firstFortnight.setText(it.budgetForFirstFortnight.toString())
                secondFortnight.setText(it.budgetForSecondFortnight.toString())
                idBudget = it.id
                addBudget.setText(R.string.update_button)
            } else {
                Snackbar.make(window.decorView.rootView, getString(R.string.error_budget2), Snackbar.LENGTH_LONG).show()
            }
        })

        addBudget.setOnClickListener {
            val budgetToAdd = Budget(firstFortnight.text.toString().toFloat(), secondFortnight.text.toString().toFloat(), currentDateInMillis)
            budgetToAdd.id = idBudget
            expenseViewModel.insertBudget(budgetToAdd)
            finish()
        }

    }

    private fun bindViews() {
        firstFortnight = findViewById(R.id.first_fortnight)
        secondFortnight = findViewById(R.id.second_fortnight)
        month = findViewById(R.id.month)
        addBudget = findViewById(R.id.addBudget)
    }
}