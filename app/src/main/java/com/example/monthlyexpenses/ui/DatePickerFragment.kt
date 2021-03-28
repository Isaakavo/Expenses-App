package com.example.monthlyexpenses.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.monthlyexpenses.expensesadd.ExpensesAddViewModel
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class DatePickerFragment(val expensesAddViewModel: ExpensesAddViewModel) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        val combinedCal = GregorianCalendar(TimeZone.getTimeZone("GMT-06:00"))
        combinedCal.set(year, month, day)
        val selectedDate = DateFormat.getDateInstance().format(combinedCal.timeInMillis)
        expensesAddViewModel.editTextDate.value = selectedDate
        expensesAddViewModel.setTimeStamp(combinedCal.timeInMillis)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        expensesAddViewModel.setTimeStamp(c.timeInMillis)
        Timber.d("Timestamp ${c.timeInMillis}")
        val picker = DatePickerDialog(activity as Context, this, year, month, day)
        return picker
    }
}