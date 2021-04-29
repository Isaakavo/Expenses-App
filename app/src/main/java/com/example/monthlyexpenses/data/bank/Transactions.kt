package com.example.monthlyexpenses.data.bank

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

//Types: Entry, exit

@Entity(
  tableName = "transactions", foreignKeys = [ForeignKey(
    entity = Banks::class, parentColumns = ["id"],
    childColumns = ["idBank"], onDelete = ForeignKey.CASCADE
  )]
)
data class Transactions(
  @ColumnInfo(name = "amount") var amount: Float? = 0.0F,
  @ColumnInfo(name = "date") var date: Long?,
  @ColumnInfo(name = "type") var type: String? = "",
  @ColumnInfo(name = "idBank") var idBank: Long = 0,
) {
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  var id: Long = 0
}
