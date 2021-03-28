package com.example.monthlyexpenses.expenses

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.data.Expenses
import com.example.monthlyexpenses.data.Items
import timber.log.Timber
import java.text.DateFormat

@BindingAdapter("formattedDate")
fun TextView.setFormattedDate(expenses: Expenses?) {
  expenses?.let {
    text = DateFormat.getDateInstance().format(expenses.date)
  }
}

@BindingAdapter("conceptText")
fun TextView.setConceptText(expenses: Expenses?) {
  Timber.d("${expenses?.concept}")
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
    text = expenses.total.toString()
  }
}

@BindingAdapter("itemConcept")
fun TextView.setItemConcept(item: Items) {
  text = item.item
}

@BindingAdapter("itemPrice")
fun TextView.setItemPrice(item: Items) {
  text = item.price
}