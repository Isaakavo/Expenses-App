package com.example.monthlyexpenses

import com.example.monthlyexpenses.data.Items
import java.text.DateFormat
import java.util.*

fun setCurrentDayFormat(): String {
  val calendar = Calendar.getInstance()
  return DateFormat.getDateInstance().format(calendar.timeInMillis)
}

val getCurrentTimestamp = Calendar.getInstance().timeInMillis

fun setDateFormat(timestamp: Long): String {
  return DateFormat.getDateInstance().format(timestamp)
}


fun getTotals(itemList: List<Items>): Float {
  var total = 0F
  for (item in itemList) {
    total += item.price.toFloat()
  }
  return total
}
