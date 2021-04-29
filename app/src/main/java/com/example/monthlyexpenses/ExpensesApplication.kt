package com.example.monthlyexpenses

import android.app.Application
import com.example.monthlyexpenses.data.ExpensesRoomDatabase
import com.example.monthlyexpenses.data.bank.BanksRepository
import com.example.monthlyexpenses.data.expenses.ExpensesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

class ExpensesApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { ExpensesRoomDatabase.getDatabase(this) }
    val expensesRepository by lazy {
        ExpensesRepository(
            database.expensesDao(),
            database.itemsDao(),
            database.budgetDao()
        )
    }
    val banksRepository by lazy {
        BanksRepository(
            database.banksDao(),
            database.transactionsDao(),
            database.monthBalance()
        )
    }
}