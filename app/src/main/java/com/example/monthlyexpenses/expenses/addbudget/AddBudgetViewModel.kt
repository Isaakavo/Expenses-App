package com.example.monthlyexpenses.expenses.addbudget

import androidx.lifecycle.*
import com.example.monthlyexpenses.data.expenses.Budget
import com.example.monthlyexpenses.data.expenses.ExpensesRepository
import com.example.monthlyexpenses.timestampForBudget
import kotlinx.coroutines.launch
import timber.log.Timber

class AddBudgetViewModel(val repository: ExpensesRepository) : ViewModel() {

    val firstHalfBudget = MutableLiveData<String>()
    val secondHalfBudget = MutableLiveData<String>()

    private val _formattedDate = MutableLiveData<String>()
    val formattedDate
        get() = _formattedDate
    val dateForDB = MutableLiveData<String>()

    private val _dismissDialog = MutableLiveData<Boolean>()
    val dismissDialog
        get() = _dismissDialog

    init {
        Timber.d("AddBudgetViewModel Created")
        _dismissDialog.value = false
        firstHalfBudget.value = "0.0"
        secondHalfBudget.value = "0.0"
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("AddBudgetViewModel Cleared")
    }

    val getBudgetByMonth = Transformations.switchMap(dateForDB) { date ->
        repository.getBudgetByMonth(date).asLiveData()
    }

    fun onSubmitClick() {
        viewModelScope.launch {
            if (firstHalfBudget.value.isNullOrEmpty()) firstHalfBudget.value = "0.0"
            if (secondHalfBudget.value.isNullOrEmpty()) secondHalfBudget.value = "0.0"

            val firstHalfFloat = firstHalfBudget.value?.toFloat()
            val secondHalfFloat = secondHalfBudget.value?.toFloat()
            val timestampBudget = timestampForBudget(dateForDB.value!!)
            val budget = Budget(firstHalfFloat!!, secondHalfFloat!!, timestampBudget)
            repository.insertBudget(budget)
            _dismissDialog.value = true
        }
    }

    fun setDialogDismissed() {
        _dismissDialog.value = false
    }

}

class AddBudgetViewModelFactory(private val repository: ExpensesRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddBudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown view model class")
    }

}