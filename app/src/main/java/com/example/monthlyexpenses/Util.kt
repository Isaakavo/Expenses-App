package com.example.monthlyexpenses

import android.content.Context
import android.content.res.Resources
import com.example.monthlyexpenses.data.expenses.Items
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

fun setDateMonthFormatted(date: String?): String {
  val selectedItemFormatter =
      SimpleDateFormat("y-MM", Locale.getDefault()).parse(date!!)
  val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
  return formatter.format(selectedItemFormatter!!)
}

fun setDayMonthFormat(timestamp: Long): String {
  return SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(timestamp)
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

fun concatenateString(string: String, res: Resources, R: Int): String {
  return res.getString(R, string)
}

fun concatenateItemPrice(item: Items, res: Resources, R: Int): String {
  return res.getString(R, item.price)
}

//Get the past month of the year
fun getSpinnerMonths(context: Context?): ArrayList<String> {
  val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
  val list = arrayListOf<String>()
  Calendar.getInstance().let { calendar ->
    val monthBefore = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val payday = calendar.get(Calendar.DAY_OF_MONTH)
    val addNewMonthFlag = calendar.getMaximum(Calendar.DAY_OF_MONTH) - 2
    if (payday > addNewMonthFlag) {
      calendar.add(Calendar.MONTH, +1)
      monthBefore.add(Calendar.MONTH, +1)
    }
    //We loop to know which month has already pass
    for (i in 1 until 12) {
      //We only want the month of current year
      val pastYear = monthBefore.get(Calendar.YEAR)
      if (pastYear == currentYear) {
        //Format the month and add to array
        list.add(formatter.format(calendar.timeInMillis))
        //Go backwards to know which month has passed
        calendar.add(Calendar.MONTH, -1)
        monthBefore.add(Calendar.MONTH, -1)
      } else break
    }
  }
  context?.let {
    list.add(context.getString(R.string.All_Expenses))
  }
  return list
}

const val ENTRY = "Income"
const val EXIT = "Exit"
fun getTransactionsTaypes(): ArrayList<String> {

  val list = arrayListOf<String>()
  list.add(ENTRY)
  list.add(EXIT)
  return list
}