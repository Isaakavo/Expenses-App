package viewmodel

import androidx.lifecycle.*
import data.ExpensesRepository
import kotlinx.coroutines.launch
import model.Budget
import model.Expenses
import model.Items
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ExpenseViewModel(private val repository: ExpensesRepository) : ViewModel() {

  private val monthTotal = MutableLiveData<Float>()
  private val _budgetTotal = MutableLiveData<Float>()
  private val _desiredDate = MutableLiveData<String>()
  val desiredDate: LiveData<String>
    get() = _desiredDate

  private val _totalFirstHalf = MutableLiveData<String>()
  val totalFirstHalf: LiveData<String>
    get() = _totalFirstHalf
  private val _totalSecondHalf = MutableLiveData<String>()
  val totalSecondHalf: LiveData<String>
    get() = _totalSecondHalf

  val budgetFirstHalf = MutableLiveData<String>()
  val budgetSecondHalf = MutableLiveData<String>()
  private val _budgetDate = MutableLiveData<String>()
  val budgetDate: LiveData<String>
    get() = _budgetDate
  private val budgetTimestamp = MutableLiveData<Long>()

  private val _budget = MutableLiveData<Budget>()
  val budget: LiveData<Budget>
    get() = _budget

  init {
    Timber.d("View model created")
    _totalFirstHalf.value = "$0.0"
    _totalSecondHalf.value = "$0.0"
    budgetFirstHalf.value = "0.0"
    budgetSecondHalf.value = "0.0"
    _budgetTotal.value = 0F
    setDesiredDate("2021-03")
  }

  fun setDesiredDate(desiredDate: String) {
    _desiredDate.value = desiredDate
  }

  val getExpenses: LiveData<List<Expenses>> = Transformations.switchMap(desiredDate) {
    when (it) {
      "All Expenses" -> allExpenses
      else -> getExpensesByDate(it)
    }
  }

  val getMonthTotals: LiveData<String> = Transformations.map(getExpenses) { expenses ->
//Formatter to only get the days of the month
    val formatter = SimpleDateFormat("dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, 0)
    calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
    val monthFirstDay = formatter.format(calendar.time)
    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    val monthLastDay = formatter.format(calendar.time)
    val monthMiddle = (monthLastDay.toInt() / 2).toString()
    expenses?.let {
      var totalOfMonthExpense = 0F
      var totalFirstHalf = 0F
      var totalSecondHalf = 0F
      for (expense in it) {
        totalOfMonthExpense += expense.total
        val dateFormatted = formatter.format(expense.date)
        if (dateFormatted in monthFirstDay..monthMiddle) {
          totalFirstHalf += expense.total
        } else if (dateFormatted in monthMiddle..monthLastDay) {
          totalSecondHalf += expense.total
        }
      }
      monthTotal.value = totalOfMonthExpense
      _totalFirstHalf.value = String.format("%.2f", totalFirstHalf)
      _totalSecondHalf.value = String.format("%.2f", totalSecondHalf)
      String.format("%.2f", totalOfMonthExpense)
    }
  }

  private val getBudgetByMonth: LiveData<Budget> = Transformations.switchMap(desiredDate) { date ->
    repository.getBudgetByMonth(date).asLiveData()
  }

  val getBudgets: LiveData<String> = Transformations.map(getBudgetByMonth) { budget ->
    var totalBudgetForMonth: Float
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val selectedItemFormatter =
        SimpleDateFormat("y-MM", Locale.getDefault()).parse(desiredDate.value!!)
    val formattedDate = formatter.format(selectedItemFormatter!!)
    Timber.d(formattedDate)
    if (budget == null) {
      budgetFirstHalf.value = "0.0"
      budgetSecondHalf.value = "0.0"
      budgetTimestamp.value = selectedItemFormatter.time
      totalBudgetForMonth = 0F
      _budgetTotal.value = totalBudgetForMonth
    } else {
      budget.let {
        budgetFirstHalf.value = String.format("%.2f", budget.budgetForFirstFortnight)
        budgetSecondHalf.value = String.format("%.2f", budget.budgetForSecondFortnight)
        _budgetDate.value = formattedDate
        budgetTimestamp.value = selectedItemFormatter.time
        totalBudgetForMonth = budget.budgetForFirstFortnight + budget.budgetForSecondFortnight
        _budgetTotal.value = totalBudgetForMonth
      }
    }
    String.format("%.2f", totalBudgetForMonth)
  }

  val remainingTotal = Transformations.switchMap(_budgetTotal) { budget ->
    Transformations.map(monthTotal) { month ->
      String.format("%.2f", budget - month)
    }
  }

  private val _eventOpenTvMonthTotal = MutableLiveData<Boolean>()
  val eventOpTvMonthTotal: LiveData<Boolean>
    get() = _eventOpenTvMonthTotal

  fun onOpenMonthTotalFragment() {
    _eventOpenTvMonthTotal.value = true
  }

  fun onOpenMonthTotalFragmentComplete() {
    _eventOpenTvMonthTotal.value = false
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

  fun insertBudget() {
    viewModelScope.launch {
      if (budgetFirstHalf.value?.isEmpty() == true) budgetFirstHalf.value = "0.0"
      if (budgetSecondHalf.value?.isEmpty() == true) budgetSecondHalf.value = "0.0"

      val budget = Budget(
          budgetFirstHalf.value.toString().toFloat(),
          budgetSecondHalf.value.toString().toFloat(),
          budgetTimestamp.value!!
      )
      Timber.d("Timestamp ${budget}")
      repository.insertBudget(budget)
    }
  }

  override fun onCleared() {
    super.onCleared()
    Timber.d("View model cleared")
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