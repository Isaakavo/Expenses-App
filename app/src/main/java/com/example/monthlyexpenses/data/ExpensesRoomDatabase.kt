package com.example.monthlyexpenses.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [Expenses::class, Items::class, Budget::class],
    version = 3,
    exportSchema = true
)
abstract class ExpensesRoomDatabase: RoomDatabase() {

    abstract fun expensesDao(): ExpensesDao
    abstract fun itemsDao(): ItemsDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `budget_table` (`budgetForFirstFortnight` REAL NOT NULL, `budgetForSecondFortnight` REAL NOT NULL, `month` INTEGER NOT NULL, PRIMARY KEY(`month`))")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `budget_new` (`budgetForFirstFortnight` REAL NOT NULL, `budgetForSecondFortnight` REAL NOT NULL, `month` INTEGER NOT NULL, PRIMARY KEY(`month`))")
                database.execSQL("INSERT INTO budget_new (budgetForFirstFortnight, budgetForSecondFortnight, month) SELECT budgetForFirstFortnight, budgetForSecondFortnight, month FROM budget_table")
                database.execSQL("DROP TABLE budget_table")
                database.execSQL("ALTER TABLE budget_new RENAME TO budget_table")
            }

        }

        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ExpensesRoomDatabase? = null

        fun getDatabase(context: Context): ExpensesRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpensesRoomDatabase::class.java,
                    "expenses_databse"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    //.addCallback(ExpensesDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }



    /*private class ExpensesDatabaseCallback(
        private val scope: CoroutineScope
    ): RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    deleteDatabase(database.expensesDao())
                }
            }
        }

        suspend fun deleteDatabase(expensesDao: ExpensesDao){
            expensesDao.deleteAll()
        }
    }*/
}