package com.example.monthlyexpenses

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.monthlyexpenses.data.expenses.Expenses
import com.example.monthlyexpenses.data.expenses.Items

@BindingAdapter("formattedDate")
fun TextView.setFormattedDate(date: Long?) {
  date?.let {
    text = setDateFormat(date)
  }
}

@BindingAdapter("formattedDateAndDays")
fun TextView.setFormattedDateAndDays(expenses: Expenses?) {
  expenses?.let {
    text = setDayMonthFormat(expenses.date)
  }
}

@BindingAdapter("conceptText")
fun TextView.setConceptText(expenses: String?) {
  if (expenses.isNullOrEmpty()) {
    backgroundTintList =
      context?.getColorStateList(R.color.red)
  } else {
    expenses.let {
      text = expenses
    }
  }
}

@BindingAdapter("total")
fun TextView.setTotal(total: Float?) {
  total?.let {
    text = concatenateString(total.toString(), context.resources, R.string.dollarsingVariable)
  }
}

@BindingAdapter("itemConcept")
fun TextView.setItemConcept(item: Items) {
  text = item.item
}

@BindingAdapter("itemPrice")
fun TextView.setItemPrice(item: Items?) {
  item?.let {
    text = concatenateItemPrice(item, context.resources, R.string.dollarsingVariable)
  }
}