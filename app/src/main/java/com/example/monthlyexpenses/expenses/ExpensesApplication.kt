package com.example.monthlyexpenses.expenses

import android.app.Application
import com.example.monthlyexpenses.BuildConfig
import com.example.monthlyexpenses.data.ExpensesRepository
import com.example.monthlyexpenses.data.ExpensesRoomDatabase
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
    val repository by lazy { ExpensesRepository(database.expensesDao(), database.itemsDao(), database.budgetDao()) }
}