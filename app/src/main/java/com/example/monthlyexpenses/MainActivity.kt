package com.example.monthlyexpenses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.monthlyexpenses.expenses.ExpensesApplication
import com.google.android.material.bottomnavigation.BottomNavigationView
import viewmodel.ExpenseViewModelFactory

class MainActivity : AppCompatActivity() {

    /*val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as ExpensesApplication).repository)
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_bank
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return ExpenseViewModelFactory((application as ExpensesApplication).repository)
    }
}