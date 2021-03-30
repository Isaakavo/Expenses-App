package com.example.monthlyexpenses

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.monthlyexpenses.data.Expenses
import com.example.monthlyexpenses.data.Items

@BindingAdapter("formattedDate")
fun TextView.setFormattedDate(expenses: Expenses?) {
  expenses?.let {
    text = setDateFormat(expenses.date)
  }
}

@BindingAdapter("formattedDateAndDays")
fun TextView.setFormattedDateAndDays(expenses: Expenses?) {
  expenses?.let {
    text = setDayMonthFormat(expenses.date)
  }
}

@BindingAdapter("conceptText")
fun TextView.setConceptText(expenses: Expenses?) {
  if (expenses?.concept.isNullOrEmpty()) {
    backgroundTintList =
        context?.getColorStateList(R.color.red)
  } else {
    expenses?.let {
      text = expenses.concept
    }
  }
}

@BindingAdapter("totalText")
fun TextView.setTotalText(expenses: Expenses?) {
  expenses?.let {
    text = concatenateString(expenses, context.resources, R.string.dollasingTotal)
  }
}

@BindingAdapter("total")
fun TextView.setTotal(expenses: Expenses?) {
  expenses?.let {
    text = concatenateString(expenses, context.resources, R.string.dollarsingVariable)
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