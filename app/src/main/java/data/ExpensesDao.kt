package data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface ExpensesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expenses): Long

    @Query("SELECT * FROM expenses_table ORDER BY date  DESC")
    fun getAllExpenses(): Flow<List<Expenses>>

    @Query("SELECT * FROM expenses_table WHERE (strftime('%Y-%m',date(date/1000, 'unixepoch'))) = :desiredDate ")
    fun getExpensesByDate(desiredDate: String): LiveData<List<Expenses>>


    /*@Query("UPDATE expenses_table SET concept = :concept, date = :date, total = :total WHERE id = :id")
    suspend fun updateExpense(concept: String, date: Long, total: Float, id: Long)*/
    @Update
    suspend fun updateExpense(expense: Expenses)

}