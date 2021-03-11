package com.example.monthlyexpenses.expenses

import adapter.ExpenseListAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.monthlyexpenses.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import data.Expenses
import data.Items
import viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

class ExpensesListFragment : Fragment(),
  ExpenseListAdapter.OnEditSelectedListener,
  ExpenseListAdapter.OnClickListener,
  AdapterView.OnItemSelectedListener {

  private val expenseViewModel: ExpenseViewModel by viewModels {
    requireActivity().defaultViewModelProviderFactory
  }

  private val firstMonthHalfString = "15/01/2021"
  private val firstMonthHalfStringStart = "01/01/2021"
  private val secondMonthHalfStringStart = "16/01/2021"
  private val secondMonthHalfString = "31/01/2021"

  private lateinit var adapter: ExpenseListAdapter
  private lateinit var totalOfMonth: TextView
  private lateinit var desiredDate: String

  private lateinit var vibrator: Vibrator

  private var totalFirstHalf = 0F
  private var totalSecondHalf = 0F

  companion object {
    const val flag = "FLAG"
    const val returnFlag = "RETURN_FLAG"
    const val newExpenseActivityRequestCode = 1
    const val editExpenseActivityRequestCode = 2
  }

  private val startForResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
      val flag = result.data?.getIntExtra(returnFlag, 0)
      if (result.resultCode == Activity.RESULT_OK && flag == newExpenseActivityRequestCode) {
        val intentExpenses =
          result.data?.extras?.getSerializable(AddNewExpense.EXTRA_EXPENSE) as Expenses
        val itemList =
          result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS)
        expenseViewModel.insert(intentExpenses, itemList as List<Items>)
        updateListByDesiredDate()
      } else if (result.resultCode == Activity.RESULT_OK && flag == editExpenseActivityRequestCode) {
        val intentExpenses =
          result.data?.extras?.getSerializable(AddNewExpense.EXTRA_EXPENSE) as Expenses
        val itemList =
          result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS)
        val itemsToDelete =
          result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS_DELETE)
        expenseViewModel.updateExpenseAndItems(intentExpenses, itemList as List<Items>)
        updateListByDesiredDate()
        expenseViewModel.deleteItems(itemsToDelete as ArrayList)
      } else if (result.resultCode != Activity.RESULT_CANCELED) {
        Snackbar.make(requireView(), "Something went wrong!", Snackbar.LENGTH_LONG).show()
      }
    }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val root = inflater.inflate(R.layout.fragment_expense_list, container, false)
    vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    bindViews(root)
    bindRecyclerView(root)

    val fab = root.findViewById<FloatingActionButton>(R.id.fab)
    fab?.setOnClickListener {
      val intent = Intent(activity, AddNewExpense::class.java)
      intent.putExtra(flag, newExpenseActivityRequestCode)
      adapter.closeMenu()
      startForResult.launch(intent)
    }
    return root
  }

  private fun bindViews(view: View?) {
    val currentMonth = view?.findViewById<Spinner>(R.id.monthSpinner)
    val arrayAdapter =
      ArrayAdapter(
        activity?.applicationContext!!,
        android.R.layout.simple_spinner_dropdown_item,
        getSpinnerMonths()
      )
    arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
    currentMonth?.adapter = arrayAdapter
    currentMonth?.onItemSelectedListener = this

    totalOfMonth = view?.findViewById(R.id.tvTotalMonth)!!
    totalOfMonth.setOnClickListener {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(150, 1))
      }
      val halfMonthTotals =
        HalfMonthTotals.newInstance(totalFirstHalf.toString(), totalSecondHalf.toString())
      halfMonthTotals.show(childFragmentManager, "Totals")
    }
  }

  private fun bindRecyclerView(view: View?) {
    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerview)
    adapter = ExpenseListAdapter(this, this)
    recyclerView?.adapter = adapter
    recyclerView?.layoutManager = LinearLayoutManager(activity?.applicationContext)

    val itemTouchHelper = ItemTouchHelper(touchHelperCallback(adapter))
    itemTouchHelper.attachToRecyclerView(recyclerView)

    recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        adapter.closeMenu()
      }
    })
  }
  override fun sendExpenseToEdit(expense: Expenses) {
    val intent = Intent(activity, AddNewExpense::class.java)
    intent.putExtra(flag, editExpenseActivityRequestCode)
    intent.putExtra(AddNewExpense.EXTRA_EXPENSE, expense)
    startForResult.launch(intent)
  }
  override fun sendExpenseToDelete(expense: Expenses) {
    expenseViewModel.deleteExpense(expense)
  }
  override fun onExpenseItemClicked(expense: Expenses) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(150, 1))
    }
    if (adapter.isMenuShown()) {
      adapter.closeMenu()
    }
    val detailsFragment =
        ExpensesDetails.newInstance(expense)
    detailsFragment.show(parentFragmentManager, "Details")
    /*expenseViewModel.getItemById(expense.id).getContentIfNotHandled()?.observe(this, {
      it?.let {
        val detailsFragment =
          ExpensesDetails.newInstance(expense.concept, expense.total, expense.date, it)
        detailsFragment.show(parentFragmentManager, "Details")
      }
    })*/
  }
  private fun touchHelperCallback(adapter: ExpenseListAdapter): ItemTouchHelper.SimpleCallback {
    return object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

      private val background = ColorDrawable(activity!!.getColor(R.color.background))
      override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                          target: RecyclerView.ViewHolder): Boolean {
        return false
      }

      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.showMenu(viewHolder.adapterPosition)
      }

      override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        when {
          dX > 0 -> {
            background.setBounds(
              itemView.left,
              itemView.top,
              itemView.left + dX.toInt(),
              itemView.bottom
            )
          }
          dX < 0 -> {
            background.setBounds(
              itemView.right + dX.toInt(),
              itemView.top,
              itemView.right,
              itemView.bottom
            )
          }
          else -> {
            background.setBounds(0, 0, 0, 0)
          }
        }
        background.draw(c)
      }
    }
  }
  private fun getSpinnerMonths(): ArrayList<String> {
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val list = arrayListOf<String>()
    Calendar.getInstance().let { calendar ->
      val monthBefore = Calendar.getInstance()
      val currentYear = calendar.get(Calendar.YEAR)
      for (i in 1 until 12) {
        val pastYear = monthBefore.get(Calendar.YEAR)
        if (pastYear == currentYear) {
          list.add(formatter.format(calendar.timeInMillis))
          calendar.add(Calendar.MONTH, -1)
          monthBefore.add(Calendar.MONTH, -1)
        }
      }
    }
    list.add("All Expenses")
    return list
  }
  //Spinner item selected
  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    val selectedItem = parent?.getItemAtPosition(position) as String

    if (selectedItem == "All Expenses") {
      expenseViewModel.allExpenses.observe(this, { expenses ->
        expenses?.let {
          adapter.submitList(it)
          var total = 0F
          for (expense in it) {
            total += expense.total
          }
          totalOfMonth.text = getString(R.string.dollarsingVariable, total.toString())
        }
      })
    } else {
      val selectedItemFormatter =
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(selectedItem)
      desiredDate = SimpleDateFormat("y-MM", Locale.getDefault()).format(selectedItemFormatter!!)
      val formatter = SimpleDateFormat("dd", Locale.getDefault())

      val firstMonthHalfStart = formatter.parse(firstMonthHalfStringStart)!!
      val firstMontHalfStartCompare = formatter.format(firstMonthHalfStart)
      val firstMonthDate = formatter.parse(firstMonthHalfString)!!
      val firstMonthCompare = formatter.format(firstMonthDate)

      val secondMonthHalfStart = formatter.parse(secondMonthHalfStringStart)!!
      val secondMonthHalfStartCompare = formatter.format(secondMonthHalfStart)
      val secondMonthHalf = formatter.parse(secondMonthHalfString)!!
      val secondMonthHalfCompare = formatter.format(secondMonthHalf)
      expenseViewModel.getExpensesByDate(desiredDate).observe(this, { expenses ->
        expenses?.let {
          adapter.submitList(it)
          var total = 0F
          totalFirstHalf = 0F
          totalSecondHalf = 0F
          for (expense in it) {
            total += expense.total
            val dateFormatted = formatter.format(expense.date)
            if (dateFormatted in firstMontHalfStartCompare..firstMonthCompare) {
              totalFirstHalf += expense.total
            } else if (dateFormatted in secondMonthHalfStartCompare..secondMonthHalfCompare) {
              totalSecondHalf += expense.total
            }
          }
          totalOfMonth.text = getString(R.string.dollarsingVariable, total.toString())
        }
      })
    }
  }
  override fun onNothingSelected(parent: AdapterView<*>?) {
    return
  }
  private fun updateListByDesiredDate() {
    expenseViewModel.getExpensesByDate(desiredDate).observe(this, {
      it?.let {
        adapter.submitList(it)
      }
    })
  }

}