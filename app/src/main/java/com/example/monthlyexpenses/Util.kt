package com.example.monthlyexpenses

import com.example.monthlyexpenses.data.Items
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun setCurrentDayFormat(): String {
  val calendar = Calendar.getInstance()
  return DateFormat.getDateInstance().format(calendar.timeInMillis)
}

val getCurrentTimestamp = Calendar.getInstance().timeInMillis

fun setDateFormat(timestamp: Long): String {
  return DateFormat.getDateInstance().format(timestamp)
}

fun setDateMonthFormatted(date: String): String {
  val selectedItemFormatter =
    SimpleDateFormat("y-MM", Locale.getDefault()).parse(date)
  val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
  return formatter.format(selectedItemFormatter!!)
}


fun timestampForBudget(date: String): Long {
  val timestampDate = SimpleDateFormat("y-MM", Locale.getDefault()).parse(date)
  return timestampDate!!.time
}

fun getTotals(itemList: List<Items>): Float {
  var total = 0F
  for (item in itemList) {
    total += item.price.toFloat()
  }
  return total
}
