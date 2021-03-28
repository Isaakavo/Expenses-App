package com.example.monthlyexpenses.expensesdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentExpensesDetailsBinding
import com.example.monthlyexpenses.expenses.ExpensesApplication
import timber.log.Timber
import java.text.DateFormat

private const val ARG_PARAM1 = "param1"

//Fragment to show the details of the expenses
class ExpensesDetails : DialogFragment() {

  private lateinit var binding: FragmentExpensesDetailsBinding
  private lateinit var expenseDetailsViewModel: ExpenseDetailsViewModel

//          private val expenseViewModel: ExpenseViewModel by viewModels {
//    requireActivity().defaultViewModelProviderFactory
//  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {

    val args = ExpensesDetailsArgs.fromBundle(requireArguments())

    val application = requireNotNull(this.activity).application

    val viewModelFactory =
      ExpenseDetailViewModelFactory((application as ExpensesApplication).repository, args.expenseId)

    expenseDetailsViewModel =
      ViewModelProvider(this, viewModelFactory).get(ExpenseDetailsViewModel::class.java)

    binding =
      DataBindingUtil.inflate(inflater, R.layout.fragment_expenses_details, container, false)
    binding.lifecycleOwner = viewLifecycleOwner
    binding.expenses = expenseDetailsViewModel

    val recyclerAdapter = ExpenseDetailsAdapter()
    binding.itemsRecyclerview.apply {
      adapter = recyclerAdapter
      isNestedScrollingEnabled = false
    }
    expenseDetailsViewModel.getItems.observe(viewLifecycleOwner, { items ->
      Timber.d("$items")
      items?.let {
        recyclerAdapter.submitList(items)
      }
    })

    return binding.root
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

//    binding.detailsConcept.text = expense.concept
//    binding.detailsDate.text = setFormattedDate(expense.date)
//    binding.totalDetails.text = getString(R.string.dollasingTotal, expense.total.toString())

    //Get the items to make text views dynamically from database
//    expenseViewModel.getItemById(expense.id).observe(this, { itemsList ->
//      itemsList?.let {
//        for (items in itemsList) {
//          createTextView(view, items.item, items.price)
//        }
//      }
//    })
  }

//  private fun createTextView(view: View, itemText: String, priceText: String) {
//    val parent = LinearLayout(context)
//    parent.layoutParams = LinearLayout.LayoutParams(
//        LinearLayout.LayoutParams.MATCH_PARENT,
//        LinearLayout.LayoutParams.WRAP_CONTENT)
//    parent.orientation = LinearLayout.HORIZONTAL
//    parent.setPadding(resources.getDimensionPixelSize(R.dimen.textPadding), 0, 0, 0)
//
//    val itemTextView = TextView(context)
//    val p: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
//    p.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.textMargin))
//    p.weight = 1F
//    itemTextView.layoutParams = p
//    itemTextView.text = itemText
//    itemTextView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
//    itemTextView.textSize = pixelsToSp(resources.getDimension(R.dimen.textSize))
//
//    val priceTextView = TextView(context)
//    val param = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
//    param.weight = 1F
//    p.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.textMargin))
//    priceTextView.layoutParams = param
//    priceTextView.text = getString(R.string.dollarsingVariable, priceText)
//    priceTextView.textSize = pixelsToSp(resources.getDimension(R.dimen.textSize))
//
//    parent.addView(itemTextView)
//    parent.addView(priceTextView)
//
//    val finalParent = view.findViewById<LinearLayout>(R.id.items_text_view)
//    finalParent?.addView(parent)
//  }

  //Transform text size to SP
  private fun pixelsToSp(px: Float): Float {
    val scaleDensity = resources.displayMetrics.scaledDensity
    return px / scaleDensity
  }

  private fun setFormattedDate(timestamp: Long?): String {
    return DateFormat.getDateInstance().format(timestamp)
  }
}