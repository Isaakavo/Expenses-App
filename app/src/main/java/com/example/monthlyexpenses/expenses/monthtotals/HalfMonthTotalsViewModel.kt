package com.example.monthlyexpenses.expenses.monthtotals

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.monthlyexpenses.data.ExpensesRepository
import java.text.SimpleDateFormat
import java.util.*

class HalfMonthTotalsViewModel(repository: ExpensesRepository, desiredDate: String) : ViewModel() {


    val firstHalf = Transformations.map(repository.getExpensesByDate(desiredDate)) { expenses ->
        val formatter = SimpleDateFormat("dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 0)
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
        val monthFirstDay = formatter.format(calendar.time)
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthLastDay = formatter.format(calendar.time)
        val monthMiddle = (monthLastDay.toInt() / 2).toString()
        var totalFirstHalf = 0F
        expenses?.let {
            for (expense in expenses) {
                val dateFormatted = formatter.format(expense.date)
                if (dateFormatted in monthFirstDay..monthMiddle) {
                    totalFirstHalf += expense.total
                }
            }
        }
        totalFirstHalf.toString()
    }

    val secondHalf = Transformations.map(repository.getExpensesByDate(desiredDate)) { expenses ->
        val formatter = SimpleDateFormat("dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, 0)
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthLastDay = formatter.format(calendar.time)
        val monthMiddle = (monthLastDay.toInt() / 2).toString()
        var totalSecondHalf = 0F
        expenses?.let {
            for (expense in expenses) {
                val dateFormatted = formatter.format(expense.date)
                if (dateFormatted in monthMiddle..monthLastDay) {
                    totalSecondHalf += expense.total
                }
            }
        }
        totalSecondHalf.toString()
    }


}

class HalfMonthTotalsFactory(val repository: ExpensesRepository, val desiredDate: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HalfMonthTotalsViewModel::class.java)) {
            return HalfMonthTotalsViewModel(repository, desiredDate) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }
}