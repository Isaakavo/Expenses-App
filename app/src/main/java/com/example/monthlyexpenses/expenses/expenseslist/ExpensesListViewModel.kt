package com.example.monthlyexpenses.expenses.expenseslist

import androidx.lifecycle.*
import com.example.monthlyexpenses.data.expenses.Expenses
import com.example.monthlyexpenses.data.expenses.ExpensesRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class ExpensesListViewModel(private val repository: ExpensesRepository) : ViewModel() {

	private val _desiredDate = MutableLiveData<String>()
	val desiredDate: LiveData<String>
		get() = _desiredDate
	private val _budgetTotal = MutableLiveData<Float>()
	private val _monthTotal = MutableLiveData<Float>()

	init {
		Timber.d("ExpensesListViewModel Created")
	}

	override fun onCleared() {
		super.onCleared()
		Timber.d("ExpensesListViewModel Cleared")
	}

	fun setDesiredDate(desiredDate: String) {
		_desiredDate.value = desiredDate
	}

	private val getBudgetByMonth = Transformations.switchMap(desiredDate) { date ->
		repository.getBudgetByMonth(date).asLiveData()
	}

	val getTotalBudgetMonth: LiveData<String> = Transformations.map(getBudgetByMonth) { budget ->
		var totalBudgetForMonth = 0F
		budget?.let {
			totalBudgetForMonth = budget.budgetForFirstFortnight + budget.budgetForSecondFortnight
		}
		_budgetTotal.value = totalBudgetForMonth
		String.format("%.2f", totalBudgetForMonth)
	}
	val getExpenses: LiveData<List<Expenses>> = Transformations.switchMap(desiredDate) { date ->
		when (date) {
			"All Expenses" -> allExpenses
			"Todos Los Gastos" -> allExpenses
			else -> getExpensesByDate(date)
		}
	}

	val getMonthTotal: LiveData<String> = Transformations.map(getExpenses) { expenses ->
		expenses?.let {
			val totalOfMonthExpense = expenses.map { it.total }.sum()
			_monthTotal.value = totalOfMonthExpense
			String.format("%.2f", totalOfMonthExpense)
		}
	}

	val remainingTotal = Transformations.switchMap(_budgetTotal) { budget ->
		Transformations.map(_monthTotal) { month ->
			var remainingTotal = 0F
			if (budget != 0F) remainingTotal = (budget - month)
			String.format("%.2f", remainingTotal)
		}
	}


	private val allExpenses: LiveData<List<Expenses>> = repository.allExpenses.asLiveData()
	private fun getExpensesByDate(desiredDate: String): LiveData<List<Expenses>> {
		return repository.getExpensesByDate(desiredDate)
	}

	private val _navigateToExpenseDetails = MutableLiveData<Long?>()
	val navigateToExpenseDetails
		get() = _navigateToExpenseDetails

	fun onExpenseSelected(id: Long) {
		_navigateToExpenseDetails.value = id
	}

	fun onExpenseDetailNavigated() {
		_navigateToExpenseDetails.value = null
	}

	private val _navigateToEditExpense = MutableLiveData<Long?>()
	val navigateToEditExpense
		get() = _navigateToEditExpense

	fun onEditSelected(id: Long) {
		_navigateToEditExpense.value = id
	}

	fun onEditNavigated() {
		_navigateToEditExpense.value = null
	}

	private val _showDeleteSnackBar = MutableLiveData<Expenses?>()
	val showDeleteSnackBar
		get() = _showDeleteSnackBar

	fun onDeleteSelected(expenses: Expenses) {
		viewModelScope.launch {
			repository.deleteExpense(expenses)
			_showDeleteSnackBar.value = expenses
		}
	}

	fun onDeleted() {
		_showDeleteSnackBar.value = null
	}

	private val _navigateToAddExpense = MutableLiveData<Boolean>()
	val navigateToAddExpense
		get() = _navigateToAddExpense

	fun onFabButtonClick() {
		_navigateToAddExpense.value = true
	}

	fun onAddExpenseNavigated() {
		_navigateToAddExpense.value = false
	}

	private val _openMonthTotals = MutableLiveData<Boolean?>()
	val openMonthTotals
		get() = _openMonthTotals

	fun onOpenMonthTotals() {
		_openMonthTotals.value = true
	}

	fun onOpenedMonthTotals() {
		_openMonthTotals.value = false
	}

}

class ExpenseListViewModelFactory(private val repository: ExpensesRepository) :
	ViewModelProvider.Factory {
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(ExpensesListViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return ExpensesListViewModel(repository) as T
		}
		throw IllegalArgumentException("Unknown view model class")
	}

}