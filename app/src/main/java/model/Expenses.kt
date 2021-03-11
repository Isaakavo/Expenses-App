package model

import androidx.room.*
import java.io.Serializable

//Data class that represents the model of expenses
@Entity(tableName = "expenses_table")
data class Expenses (
    @ColumnInfo(name = "concept") val concept: String,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "expenseType") val expenseType: String,
    @ColumnInfo(name = "total") val total: Float
): Serializable{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")var id: Long = 0

    @Ignore
    var showMenu: Boolean = false


}