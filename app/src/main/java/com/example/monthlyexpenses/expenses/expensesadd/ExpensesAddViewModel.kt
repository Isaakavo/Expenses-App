package com.example.monthlyexpenses.expenses.expensesadd

import android.view.View
import androidx.lifecycle.*
import com.example.monthlyexpenses.data.expenses.Expenses
import com.example.monthlyexpenses.data.expenses.ExpensesRepository
import com.example.monthlyexpenses.data.expenses.Items
import com.example.monthlyexpenses.getCurrentTimestamp
import com.example.monthlyexpenses.getTotals
import com.example.monthlyexpenses.setCurrentDayFormat
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class ExpensesAddViewModel(private val repository: ExpensesRepository, val expenseId: Long) :
    ViewModel() {

    private var itemList: MutableList<Items> = arrayListOf()
    private val itemListToDelete: ArrayList<Items> = arrayListOf()

    var isConceptFill = false
    val concept = MutableLiveData<String>()
    val editTextDate = MutableLiveData<String>()

    val expense: LiveData<Expenses>? = repository.getExpenseById(expenseId)

    val items = repository.getItemById(expenseId)

    private val _itemListLiveData = MutableLiveData<List<Items>>()
    val itemListLiveData
        get() = _itemListLiveData

    val showTotal = MutableLiveData<Int>()

    val itemsTotal: LiveData<String> = Transformations.map(_itemListLiveData) { itemList ->
        var itemTotal = 0F
        itemList?.let {
            itemTotal = itemList.map { it.price.toFloat() }.sum()
            if (itemTotal == 0.0F) showTotal.value = View.INVISIBLE
            else showTotal.value = View.VISIBLE
        }
        String.format("%.2f", itemTotal)
    }

    fun addEditText() {
        itemList.add(Items())
        _itemListLiveData.value = itemList
    }

    fun removeEditText() {
        if (itemList.size > 1) {
            itemListToDelete.add(itemList.last())
            itemList.removeLast()
            _itemListLiveData.value = itemList
        }
    }

    fun setItemList(list: List<Items>) {
        itemList = list.toMutableList()
        _itemListLiveData.value = itemList
    }

    private val _showDatePicker = MutableLiveData<Boolean>()
    val showDatePicker
        get() = _showDatePicker
    private val _timestamp = MutableLiveData<Long>()

    fun setTimeStamp(timestamp: Long) {
        _timestamp.value = timestamp
    }

    fun displayDatePicker() {
        _showDatePicker.value = true
    }

    fun hideDatePicker() {
        _showDatePicker.value = false
    }

    private val _navigateBackToHome = MutableLiveData<Boolean>()
    val navigateBackToHome
        get() = _navigateBackToHome

    fun onSendToDatabase() {
        viewModelScope.launch {
            if (isConceptFill) {
                val total = getTotals(itemList)
                val expense = Expenses(concept.value!!, _timestamp.value!!, "expense", total)
                expense.id = expenseId
                repository.insertExpenseAndItem(expense, itemList)
                if (itemListToDelete.isNotEmpty()) {
                    repository.deleteItem(itemListToDelete)
                }
                _navigateBackToHome.value = true
            } else {
                concept.value = ""
            }
        }
    }

    fun onDatabaseSent() {
        _navigateBackToHome.value = false
    }

    init {
        Timber.d("ExpenseDetailsViewModel Created")
        addEditText()
        editTextDate.value = setCurrentDayFormat()
        _timestamp.value = getCurrentTimestamp
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("ExpenseDetailsViewModel Cleared")
    }

}

class ExpensesAddViewModelFactory(private val repository: ExpensesRepository, val expenseId: Long) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(ExpensesAddViewModel::class.java)) {
            return ExpensesAddViewModel(repository, expenseId) as T
        }
        throw IllegalArgumentException("unknown view model class")
    }

}