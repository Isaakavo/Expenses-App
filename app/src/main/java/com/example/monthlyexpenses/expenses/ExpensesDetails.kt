package com.example.monthlyexpenses.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.monthlyexpenses.R
import model.Expenses
import viewmodel.ExpenseViewModel
import java.text.DateFormat

private const val ARG_PARAM1 = "param1"

//Fragment to show the details of the expenses
class ExpensesDetails : DialogFragment() {

  private val expenseViewModel: ExpenseViewModel by viewModels {
    requireActivity().defaultViewModelProviderFactory
  }

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_expenses_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)
  }

  //Make the card size of the screen
  override fun onStart() {
    super.onStart()
    dialog?.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )
  }

  private fun setupView(view: View) {
    val detailsConcept = view.findViewById<TextView>(R.id.details_concept)
    val detailsDate = view.findViewById<TextView>(R.id.details_date)
    val totalDetails = view.findViewById<TextView>(R.id.totalDetails)

    val expense = arguments?.getSerializable(ARG_PARAM1) as Expenses

    detailsConcept.text = expense.concept
    detailsDate.text = setFormattedDate(expense.date)
    totalDetails.text = getString(R.string.dollasingTotal, expense.total.toString())

    //Get the items to make text views dynamically from database
    expenseViewModel.getItemById(expense.id).observe(this, { itemsList ->
      itemsList?.let {
        for (items in itemsList) {
          createTextView(view, items.item, items.price)
        }
      }
    })
  }

  private fun createTextView(view: View, itemText: String, priceText: String) {
    val parent = LinearLayout(context)
    parent.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT)
    parent.orientation = LinearLayout.HORIZONTAL
    parent.setPadding(resources.getDimensionPixelSize(R.dimen.textPadding), 0, 0, 0)

    val itemTextView = TextView(context)
    val p: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
    p.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.textMargin))
    p.weight = 1F
    itemTextView.layoutParams = p
    itemTextView.text = itemText
    itemTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
    itemTextView.textSize = pixelsToSp(resources.getDimension(R.dimen.textSize))

    val priceTextView = TextView(context)
    val param = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
    param.weight = 1F
    p.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.textMargin))
    priceTextView.layoutParams = param
    priceTextView.text = getString(R.string.dollarsingVariable, priceText)
    priceTextView.textSize = pixelsToSp(resources.getDimension(R.dimen.textSize))

    parent.addView(itemTextView)
    parent.addView(priceTextView)

    val finalParent = view.findViewById<LinearLayout>(R.id.items_text_view)
    finalParent?.addView(parent)
  }

  //Transform text size to SP
  private fun pixelsToSp(px: Float): Float {
    val scaleDensity = resources.displayMetrics.scaledDensity
    return px / scaleDensity
  }

  private fun setFormattedDate(timestamp: Long?): String {
    return DateFormat.getDateInstance().format(timestamp)
  }

  //We create the dialog with the data we want from here
  companion object {
    @JvmStatic
    fun newInstance(expense: Expenses) =
        ExpensesDetails().apply {
          arguments = Bundle().apply {
            putSerializable(ARG_PARAM1, expense)
          }
          val fragment = ExpensesDetails()
          fragment.arguments = arguments
          return fragment
        }
  }
}