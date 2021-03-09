package data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Items)

    @Query("SELECT * FROM items_table WHERE expenseId = :id")
    fun getItemByID(id: Long): LiveData<List<Items>>

    @Query("SELECT * FROM items_table")
    fun getAllItems(): Flow<List<Items>>

    @Update
    suspend fun updateItems(item: Items)

    @Delete
    suspend fun deleteItem(item: ArrayList<Items>)
}