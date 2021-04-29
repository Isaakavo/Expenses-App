package com.example.monthlyexpenses.data.bank

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "month_balance")
data class MonthBalance(
    @ColumnInfo(name = "idBank") var idBank: Long = 0,
    @ColumnInfo(name = "monthStart") var monthStart: Long = 0,
    @ColumnInfo(name = "monthEnd") var monthEnd: Long = 0
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}
