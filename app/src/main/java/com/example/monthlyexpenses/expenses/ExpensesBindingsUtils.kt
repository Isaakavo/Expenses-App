package com.example.monthlyexpenses.expenses

import android.widget.TextView
import androidx.databinding.BindingAdapter
import model.Expenses
import timber.log.Timber
import java.text.DateFormat

@BindingAdapter("formattedDate")
fun TextView.setFormattedDate(item: Expenses) {
  Timber.d(item.toString())
  text = DateFormat.getDateInstance().format(item.date)
}

@BindingAdapter("conceptText")
fun TextView.setConceptText(item: Expenses) {
  text = item.concept
}

@BindingAdapter("totalText")
fun TextView.setTotalText(item: Expenses) {
  text = item.total.toString()
}