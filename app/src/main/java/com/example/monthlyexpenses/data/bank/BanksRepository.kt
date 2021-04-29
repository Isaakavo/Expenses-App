package com.example.monthlyexpenses.data.bank

class BanksRepository(
  private val banksDao: BanksDao,
  private val transactionsDao: TransactionsDao,
  private val monthBalanceDao: MonthBalanceDao
) {

	suspend fun insert(banks: Banks): Long {
		return banksDao.insert(banks)
	}

	suspend fun insert(transactions: Transactions): Long {
		return transactionsDao.insert(transactions)
	}
}