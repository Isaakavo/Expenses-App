package com.example.monthlyexpenses.expenses.expensesdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.monthlyexpenses.data.expenses.Expenses
import com.example.monthlyexpenses.data.expenses.ExpensesRepository
import com.example.monthlyexpenses.data.expenses.Items
import timber.log.Timber

class ExpenseDetailsViewModel(repository: ExpensesRepository, val expenseId: Long) : ViewModel() {

    val expenses: LiveData<Expenses>?

    init {
        Timber.d("ExpenseDetailsViewModel Created")
        expenses = repository.getExpenseById(expenseId)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("ExpenseDetailsViewModel Cleared")
    }

    val getItems: LiveData<List<Items>> =
        repository.getItemById(expenseId)
}


class ExpenseDetailViewModelFactory(
    private val repository: ExpensesRepository,
    private val expenseId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseDetailsViewModel(repository, expenseId) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }

}