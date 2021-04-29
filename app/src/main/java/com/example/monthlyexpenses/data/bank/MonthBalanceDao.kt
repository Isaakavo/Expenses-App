package com.example.monthlyexpenses.data.bank

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface MonthBalanceDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(monthBalance: MonthBalance): Long

}