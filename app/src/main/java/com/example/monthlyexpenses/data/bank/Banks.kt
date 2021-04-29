package com.example.monthlyexpenses.data.bank

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banks")
data class Banks(
  @ColumnInfo(name = "bank") val bank: String = "",
  @ColumnInfo(name = "type") val type: String = "",
  @ColumnInfo(name = "cutoffDate") val cutoffDate: Long?,
  @ColumnInfo(name = "paydayLimit") val paydayLimit: Long?
) {
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  var id: Long = 0
}
