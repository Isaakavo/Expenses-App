package UI

import android.view.View
import android.widget.AdapterView
import viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

class MonthSpinner(private val expenseViewModel: ExpenseViewModel) : AdapterView.OnItemSelectedListener {

  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    val selectedItem = parent?.getItemAtPosition(position) as String
    if (selectedItem == "All Expenses") {
      expenseViewModel.setDesiredDate("All Expenses")
    } else {
      val selectedItemFormatter =
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(selectedItem)!!
      val desiredDate = SimpleDateFormat("y-MM", Locale.getDefault()).format(selectedItemFormatter)
      expenseViewModel.setDesiredDate(desiredDate)
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
    return
  }

  companion object {
    //Get the past month of the year
    fun getSpinnerMonths(): ArrayList<String> {
      val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
      val list = arrayListOf<String>()
      Calendar.getInstance().let { calendar ->
        val monthBefore = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
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
          }
        }
      }
      //list.add("All Expenses")
      return list
    }
  }
}