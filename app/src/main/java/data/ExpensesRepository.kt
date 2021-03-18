package data

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import model.Budget
import model.Expenses
import model.Items

class ExpensesRepository(private val expensesDao: ExpensesDao, private val itemsDao: ItemsDao,
                         private val budgetDao: BudgetDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allExpenses: Flow<List<Expenses>> = expensesDao.getAllExpenses()

    val allItems: Flow<List<Items>> = itemsDao.getAllItems()

    @WorkerThread
    suspend fun deleteExpense(expense: Expenses) {
        return expensesDao.deleteExpense(expense)
    }

    @WorkerThread
    suspend fun deleteItem(items: ArrayList<Items>) {
        return itemsDao.deleteItem(items)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(expense: Expenses): Long {
        return expensesDao.insert(expense)
    }

    @WorkerThread
    suspend fun insertExpenseAndItem(expense: Expenses, items: List<Items>){

        val id = expensesDao.insert(expense)
        for (item: Items in items) {
            item.expenseId = id
            itemsDao.insert(item)
        }

    }

    //First update the expense then the items
    suspend fun updateExpenseAndItems(expense: Expenses, items: List<Items>) {
        expensesDao.updateExpense(expense)
        for (item in items) {
            if (item.id != 0L) itemsDao.updateItems(item)
            else {
                item.expenseId = expense.id
                itemsDao.insert(item)
            }
        }
    }

    @WorkerThread
    fun getItemById(id: Long): Flow<List<Items>> {
        return itemsDao.getItemByID(id)
    }

    @WorkerThread
    fun getExpensesByDate(desiredDate: String?): LiveData<List<Expenses>> {
        return expensesDao.getExpensesByDate(desiredDate)
    }

    @WorkerThread
    suspend fun insertBudget(budget: Budget){
        return budgetDao.insert(budget)
    }

    @WorkerThread
    fun getBudgetByMonth(desiredMonth: String): Flow<Budget> {
        return budgetDao.getBudgetByMonth(desiredMonth)
    }

}