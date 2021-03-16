package com.example.monthlyexpenses.expenses

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import com.example.monthlyexpenses.R
import com.google.android.material.snackbar.Snackbar
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

    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)

        firstFortnight = findViewById(R.id.first_fortnight)
        secondFortnight = findViewById(R.id.second_fortnight)
        month = findViewById(R.id.month)
        addBudget = findViewById(R.id.addBudget)

        val currentDate = formatter.format(Calendar.getInstance().timeInMillis)

        month.setText(currentDate)

        expenseViewModel.getBudgetByMonth(month.text.toString()).observe(this, {
            Log.d("puito", it.isEmpty().toString())
            if (it.isEmpty()){
                Snackbar.make(window.decorView.rootView, "Nothing on Database", Snackbar.LENGTH_LONG).show()
            }
        })

    }
}