package model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_table")
data class Budget(
        @ColumnInfo(name = "budgetForFirstFortnight")
        val budgetForFirstFortnight: Float,
        @ColumnInfo(name = "budgetForSecondFortnight")
        val budgetForSecondFortnight: Float,
        @ColumnInfo(name = "month")
        val month: Long
        ){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}