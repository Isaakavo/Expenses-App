package com.example.monthlyexpenses.data.expenses

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow


//interface to make database query's
@Dao
interface ExpensesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expenses): Long

    @Query("SELECT * FROM expenses_table ORDER BY date  DESC")
    fun getAllExpenses(): Flow<List<Expenses>>

    @Query("SELECT * FROM expenses_table WHERE (strftime('%Y-%m',date(date/1000, 'unixepoch'))) = :desiredDate ORDER BY date  DESC")
    fun getExpensesByDate(desiredDate: String): LiveData<List<Expenses>>

    @Query("SELECT * FROM expenses_table WHERE id = :expenseId")
    fun getExpenseById(expenseId: Long?): LiveData<Expenses>?

    @Update
    suspend fun updateExpense(expense: Expenses)

    @Delete
    suspend fun deleteExpense(expense: Expenses)

}