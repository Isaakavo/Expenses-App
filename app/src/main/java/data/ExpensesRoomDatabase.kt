package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import model.Expenses
import model.Items


@Database(entities = [Expenses::class, Items::class], version = 1, exportSchema = false)
abstract class ExpensesRoomDatabase: RoomDatabase() {

    abstract fun expensesDao(): ExpensesDao
    abstract fun itemsDao(): ItemsDao

    companion object{
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ExpensesRoomDatabase? = null

        fun getDatabase(context: Context): ExpensesRoomDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpensesRoomDatabase::class.java,
                    "expenses_databse"
                )
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