package com.example.monthlyexpenses.data.bank

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BanksDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(banks: Banks): Long

	@Query("SELECT * FROM banks WHERE id = :bankId")
	fun getBankById(bankId: Long): LiveData<Banks>

}