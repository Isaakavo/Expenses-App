package data

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class ExpensesRepository(private val expensesDao: ExpensesDao, private val itemsDao: ItemsDao) {
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
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(expense: Expenses): Long {
        return expensesDao.insert(expense)
    }

    @WorkerThread
    suspend fun insertExpenseAndItem(expense: Expenses, items: List<Items>){

        val id = expensesDao.insert(expense)
        for (item: Items in items){
            item.expenseId = id
            Log.d("repository", "The id to insert ${item.id}")
            Log.d("repository", "The item to insert $item")
            itemsDao.insert(item)
        }

    }
    suspend fun updateExpenseAndItems(expense: Expenses, items: List<Items>){
        /*expensesDao.updateExpense(expense.concept, expense.date, expense.total, expense.id)*/
        expensesDao.updateExpense(expense)
        Log.d("upsert", items.toString())
        for (item in items){
            if (item.id != 0L) itemsDao.updateItems(item)
            else {
                item.expenseId = expense.id
                itemsDao.insert(item)
            }
        }
    }

    @WorkerThread
    fun getItemById(id: Long): LiveData<List<Items>> {
        return itemsDao.getItemByID(id)
    }

    @WorkerThread
    fun getExpensesByDate(desiredDate: String): LiveData<List<Expenses>> {
        return expensesDao.getExpensesByDate(desiredDate)
    }

}