package com.example.monthlyexpenses.data.bank

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface TransactionsDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(transactions: Transactions): Long

	@Query("SELECT * FROM transactions")
	fun getAllTransactions(): LiveData<List<Transactions>>

}