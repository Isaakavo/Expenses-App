package data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import model.Budget

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget)
    @Query("SELECT * FROM budget_table WHERE (strftime('%Y-%m',date(month/1000, 'unixepoch'))) = :desiredMonth")
    fun getBudgetByMonth(desiredMonth: String): Flow<List<Budget>>

}