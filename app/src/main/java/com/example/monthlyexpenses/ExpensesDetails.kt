package com.example.monthlyexpenses

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import data.Items
import java.text.DateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class ExpensesDetails : DialogFragment() {

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

    detailsConcept.text = arguments?.getString(ARG_PARAM1)
    detailsDate.text = setFormattedDate(arguments?.getLong(ARG_PARAM2))

    val itemsList = arguments?.getParcelableArrayList<Items>(ARG_PARAM3)

    for (items in itemsList!!) {
      Log.d("items", items.toString())
      createTextView(view, items.item, items.price)
    }
  }

  private fun createTextView(view: View, itemText: String, priceText: String) {
    val parent = LinearLayout(context)
    parent.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT)
    parent.orientation = LinearLayout.HORIZONTAL
    parent.gravity = Gravity.CENTER

    parent.setPadding(resources.getDimensionPixelSize(R.dimen.textPadding), 0, 0, resources.getDimensionPixelSize(R.dimen.textMargin))

    val itemTextView = TextView(context)
    val p: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
    //p.setMargins(resources.getDimensionPixelSize(R.dimen.textMargin), 0,0,resources.getDimensionPixelSize(R.dimen.textBottomMargin))
    itemTextView.layoutParams = p
    itemTextView.text = itemText
    itemTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
    itemTextView.textSize = resources.getDimension(R.dimen.textSize)

    val priceTextView = TextView(context)
    val param = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    param.setMargins(resources.getDimensionPixelSize(R.dimen.marginComment), 0, 0, 0)
    priceTextView.layoutParams = param
    priceTextView.text = getString(R.string.dollarsingVariable, priceText)
    priceTextView.textSize = resources.getDimension(R.dimen.textSize)

    parent.addView(itemTextView)
    parent.addView(priceTextView)

    val finalParent = view.findViewById<LinearLayout>(R.id.items_text_view)
    finalParent?.addView(parent)
  }

  private fun setFormattedDate(timestamp: Long?): String {
    return DateFormat.getDateInstance().format(timestamp)
  }

  companion object {

    @JvmStatic
    fun newInstance(concept: String, date: Long, items: List<Items>) =
        ExpensesDetails().apply {
          arguments = Bundle().apply {
            putString(ARG_PARAM1, concept)
            putLong(ARG_PARAM2, date)
            putParcelableArrayList(ARG_PARAM3, items as ArrayList<out Parcelable>)
          }
          val fragment = ExpensesDetails()
          fragment.arguments = arguments
          return fragment
        }
  }
}