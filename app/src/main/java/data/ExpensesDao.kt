package data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface ExpensesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expenses): Long

    @Query("SELECT * FROM expenses_table ORDER BY date  DESC")
    fun getAllExpenses(): Flow<List<Expenses>>

    @Query("DELETE FROM expenses_table")
    suspend fun deleteAll()

    /*@Query("UPDATE expenses_table SET concept = :concept, date = :date, total = :total WHERE id = :id")
    suspend fun updateExpense(concept: String, date: Long, total: Float, id: Long)*/
    @Update
    suspend fun updateExpense(expense: Expenses)

}