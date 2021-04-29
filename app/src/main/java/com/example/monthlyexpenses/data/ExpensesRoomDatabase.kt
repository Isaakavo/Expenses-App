package com.example.monthlyexpenses.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.monthlyexpenses.data.bank.*
import com.example.monthlyexpenses.data.expenses.*


@Database(
	entities = [Expenses::class, Items::class, Budget::class,
		Banks::class, Transactions::class, MonthBalance::class],
	version = 4,
	exportSchema = true
)
abstract class ExpensesRoomDatabase : RoomDatabase() {

	abstract fun expensesDao(): ExpensesDao
	abstract fun itemsDao(): ItemsDao
	abstract fun budgetDao(): BudgetDao

	abstract fun banksDao(): BanksDao
	abstract fun transactionsDao(): TransactionsDao
	abstract fun monthBalance(): MonthBalanceDao

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

		private val MIGRATION_3_4 = object : Migration(3, 4) {
			override fun migrate(database: SupportSQLiteDatabase) {
				database.execSQL("CREATE TABLE IF NOT EXISTS `banks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bank` TEXT NOT NULL, `type` TEXT NOT NULL, `cutoffDate` INTEGER, `paydayLimit` INTEGER)")
				database.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` REAL, `date` INTEGER, `type` TEXT, `idBank` INTEGER NOT NULL, FOREIGN KEY(`idBank`) REFERENCES `banks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
				database.execSQL("CREATE TABLE IF NOT EXISTS `month_balance` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `idBank` INTEGER NOT NULL, `monthStart` INTEGER NOT NULL, `monthEnd` INTEGER NOT NULL)")
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
					.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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