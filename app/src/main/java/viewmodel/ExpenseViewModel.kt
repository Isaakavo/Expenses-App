package viewmodel

import androidx.lifecycle.*
import data.ExpensesRepository
import kotlinx.coroutines.launch
import model.Budget
import model.Expenses
import model.Items


class ExpenseViewModel(private val repository: ExpensesRepository): ViewModel() {

  val monthTotal = MutableLiveData<Float>()
  val budgetTotal = MutableLiveData<Float>()
  val desiredDate = MutableLiveData<String>()
  val getExpenses: LiveData<List<Expenses>> = Transformations.switchMap(desiredDate) {
    when (it) {
      "All Expenses" -> allExpenses
      else -> getExpensesByDate(it)
    }
  }
  val getBudgets: LiveData<Budget> = Transformations.switchMap(desiredDate) {
    getBudgetByMonth(it)
  }

  val remainingTotal = Transformations.switchMap(budgetTotal) { budget ->
    Transformations.map(monthTotal) { month ->
      budget - month
    }
  }
  private val allExpenses: LiveData<List<Expenses>> = repository.allExpenses.asLiveData()

  private fun getExpensesByDate(desiredDate: String?): LiveData<List<Expenses>> {
    return repository.getExpensesByDate(desiredDate)
  }

  // Launching a new coroutine to insert the data in a non-blocking way
  fun insert(expense: Expenses, item: List<Items>) = viewModelScope.launch {
    repository.insertExpenseAndItem(expense, item)
  }

  fun getItemById(id: Long): LiveData<List<Items>> {
    return repository.getItemById(id).asLiveData()
  }

  fun updateExpenseAndItems(expense: Expenses, item: List<Items>) = viewModelScope.launch {
    repository.updateExpenseAndItems(expense, item)
  }

  fun deleteExpense(expense: Expenses) {
    viewModelScope.launch {
      repository.deleteExpense(expense)
    }
  }

  fun deleteItems(items: ArrayList<Items>) {
    viewModelScope.launch {
      repository.deleteItem(items)
    }
  }

  fun insertBudget(budget: Budget){
    viewModelScope.launch {
      repository.insertBudget(budget)
    }
  }

  fun getBudgetByMonth(desiredMonth: String): LiveData<Budget> {
    return repository.getBudgetByMonth(desiredMonth).asLiveData()
  }
}

class ExpenseViewModelFactory(private val repository: ExpensesRepository): ViewModelProvider.Factory{
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)){
      @Suppress("UNCHECKED_CAST")
      return ExpenseViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown view model class")
  }
}