package UI

import android.view.View
import android.widget.AdapterView
import viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

class MonthSpinner(private val expenseViewModel: ExpenseViewModel) : AdapterView.OnItemSelectedListener {

  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    val selectedItem = parent?.getItemAtPosition(position) as String
    //If the user wants to see all the expenses
    if (selectedItem == "All Expenses") {
      expenseViewModel.setDesiredDate("All Expenses")
    } else {
      //We only display the expenses of the desired month
      val selectedItemFormatter =
          SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(selectedItem)!!
      //We use a different format to make a db query
      val desiredDate = SimpleDateFormat("y-MM", Locale.getDefault()).format(selectedItemFormatter)
      expenseViewModel.setDesiredDate(desiredDate)
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
    return
  }

}