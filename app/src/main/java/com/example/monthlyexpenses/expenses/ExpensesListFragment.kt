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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import model.Expenses
import model.Items
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

  //Variables to calculate half months
  private val firstMonthHalfString = "15/01/2021"
  private val firstMonthHalfStringStart = "01/01/2021"
  private val secondMonthHalfStringStart = "16/01/2021"
  private val secondMonthHalfString = "31/01/2021"

  private lateinit var recyclerAdapter: ExpenseListAdapter
  private lateinit var totalOfMonth: TextView
  private lateinit var desiredDate: String
  private lateinit var fabMenu: FloatingActionButton
  private lateinit var fabLayoutAddExpense: LinearLayout
  private lateinit var fabAddExpense: FloatingActionButton
  private lateinit var fabLayoutAddIncome: LinearLayout
  private lateinit var fabAddBudget: FloatingActionButton
  lateinit var shadowView: View

  var clicked = true

  private lateinit var vibrator: Vibrator

  private var totalFirstHalf = 0F
  private var totalSecondHalf = 0F

  companion object {
    const val flag = "FLAG"
    const val returnFlag = "RETURN_FLAG"
    const val newExpenseActivityRequestCode = 1
    const val editExpenseActivityRequestCode = 2
    const val newBudgetActivityRequestCode = 3
  }

  //From here we can decide what to do. If we want to add new expense to DB or edit one existing expense
  private val startAddNewExpenseForResult =
      registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val flag = result.data?.getIntExtra(returnFlag, 0)
        if (result.resultCode == Activity.RESULT_OK && flag == newExpenseActivityRequestCode) {
          //Get the data from the activity
          val intentExpenses =
              result.data?.extras?.getSerializable(AddNewExpense.EXTRA_EXPENSE) as Expenses
          val itemList =
              result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS)
          //Insert data to data base
          expenseViewModel.insert(intentExpenses, itemList as List<Items>)
          updateListByDesiredDate()
        } else if (result.resultCode == Activity.RESULT_OK && flag == editExpenseActivityRequestCode) {
          val intentExpenses =
              result.data?.extras?.getSerializable(AddNewExpense.EXTRA_EXPENSE) as Expenses
          val itemList =
              result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS)
          //If the user want to delete an item we recover wich data and the we delete it using
          //delete items method from view model
          val itemsToDelete =
              result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS_DELETE)
          expenseViewModel.updateExpenseAndItems(intentExpenses, itemList as List<Items>)
          updateListByDesiredDate()
          expenseViewModel.deleteItems(itemsToDelete as ArrayList)
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
          Snackbar.make(requireView(), "Something went wrong!", Snackbar.LENGTH_LONG).show()
        }
      }
  private val startAddNewBudgetForResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
      val flag = result.data?.getIntExtra(returnFlag, 0)

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

    fabMenu.setOnClickListener {
      if (clicked) {
        showFabMenu()
      } else {
        closeFabMenu()
      }
    }
    fabAddExpense.setOnClickListener{
      val intent = Intent(activity, AddNewExpense::class.java)
      intent.putExtra(flag, newExpenseActivityRequestCode)
      recyclerAdapter.closeMenu()
      startAddNewExpenseForResult.launch(intent)
    }

    fabAddBudget.setOnClickListener{
      val intent = Intent(activity, AddBudget::class.java)
      intent.putExtra(flag, newBudgetActivityRequestCode)
      recyclerAdapter.closeMenu()
      startActivity(intent)
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

    fabMenu = view?.findViewById(R.id.fabMenu)!!
    fabLayoutAddExpense = view.findViewById(R.id.fabLayoutAddExpense)
    fabAddExpense = view.findViewById(R.id.fabAddExpense)
    fabLayoutAddIncome = view.findViewById(R.id.fabLayoutAddIncome)
    fabAddBudget = view.findViewById(R.id.fabAddBudget)
    shadowView = view.findViewById(R.id.shadowView)

    totalOfMonth = view.findViewById(R.id.tvTotalMonth)!!
    //When user clicks on total of month a fragment is displayed to show the expenses by half month
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
    recyclerAdapter = ExpenseListAdapter(this, this)
    recyclerView?.apply {
      adapter = recyclerAdapter
      layoutManager = LinearLayoutManager(activity?.applicationContext)
      setItemViewCacheSize(15)
      setHasFixedSize(true)
    }
    //Set the adapter to swipe function
    val itemTouchHelper = ItemTouchHelper(touchHelperCallback(recyclerAdapter))
    itemTouchHelper.attachToRecyclerView(recyclerView)

    recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        recyclerAdapter.closeMenu()
      }
    })
  }

  private fun showFabMenu() {
    fabLayoutAddExpense.visibility = View.VISIBLE
    fabLayoutAddIncome.visibility = View.VISIBLE
    fabMenu.animate().rotationBy(45F)
    fabLayoutAddExpense.animate().translationY(180F)
    fabLayoutAddIncome.animate().translationY(360F)
    shadowView.visibility = View.VISIBLE
    clicked = false
  }

  private fun closeFabMenu() {
    fabMenu.animate().rotationBy(45F)
    fabLayoutAddIncome.animate().translationY(0F)
    fabLayoutAddExpense.animate().translationY(0F)
    fabLayoutAddExpense.visibility = View.GONE
    fabLayoutAddIncome.visibility = View.GONE
    shadowView.visibility = View.GONE
    clicked = true
  }

  //Interface from ExpenseListAdapter to start edit activity with data to edit
  override fun sendExpenseToEdit(expense: Expenses) {
    val intent = Intent(activity, AddNewExpense::class.java)
    intent.putExtra(flag, editExpenseActivityRequestCode)
    intent.putExtra(AddNewExpense.EXTRA_EXPENSE, expense)
    startAddNewExpenseForResult.launch(intent)
  }

  //Interface from ExpenseListAdapter to delete an expense
  override fun sendExpenseToDelete(expense: Expenses) {
    expenseViewModel.deleteExpense(expense)
  }

  //Interface from ExpenseListAdapter to open the expense dialog details
  override fun onExpenseItemClicked(expense: Expenses) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(150, 1))
    }
    if (recyclerAdapter.isMenuShown()) {
      recyclerAdapter.closeMenu()
    }
    val detailsFragment =
        ExpensesDetails.newInstance(expense)
    detailsFragment.show(parentFragmentManager, "Details")
  }
  private fun touchHelperCallback(adapter: ExpenseListAdapter): ItemTouchHelper.SimpleCallback {
    return object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

      private val background = ColorDrawable(activity!!.getColor(R.color.background))
      override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                          target: RecyclerView.ViewHolder): Boolean {
        return false
      }

      //Show the menu when user ends swipe
      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.showMenu(viewHolder.adapterPosition)
      }

      //Draw color while user is swiping the item
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

  //Get the past month of the year
  private fun getSpinnerMonths(): ArrayList<String> {
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
    list.add("All Expenses")
    return list
  }
  //Spinner item selected
  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    val selectedItem = parent?.getItemAtPosition(position) as String
    //If the user wants to see all the expenses
    if (selectedItem == "All Expenses") {
      expenseViewModel.allExpenses.observe(this, { expenses ->
        expenses?.let {
          recyclerAdapter.submitList(it)
          var total = 0F
          for (expense in it) {
            total += expense.total
          }
          totalOfMonth.text = getString(R.string.dollarsingVariable, total.toString())
        }
      })
    } else {
      //We only display the expenses of the desired month
      val selectedItemFormatter =
          SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(selectedItem)
      //We use a different format to make a db query
      desiredDate = SimpleDateFormat("y-MM", Locale.getDefault()).format(selectedItemFormatter!!)
      //Formatter to only get the daysof the month
      val formatter = SimpleDateFormat("dd", Locale.getDefault())

      //Format dates to make the sum of total from 1st until 15th
      val firstMonthHalfStart = formatter.parse(firstMonthHalfStringStart)!!
      val firstMontHalfStartCompare = formatter.format(firstMonthHalfStart)
      val firstMonthDate = formatter.parse(firstMonthHalfString)!!
      val firstMonthCompare = formatter.format(firstMonthDate)
      //Format dates to make the sum of total from 16th until 31th
      val secondMonthHalfStart = formatter.parse(secondMonthHalfStringStart)!!
      val secondMonthHalfStartCompare = formatter.format(secondMonthHalfStart)
      val secondMonthHalf = formatter.parse(secondMonthHalfString)!!
      val secondMonthHalfCompare = formatter.format(secondMonthHalf)
      //Make the query by desired date
      expenseViewModel.getExpensesByDate(desiredDate).observe(this, { expenses ->
        expenses?.let {
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
          Log.d("date", it.toString())
          recyclerAdapter.submitList(it)
        }
      })
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
    return
  }

  //Update the list by the last selected date. Variable desiredDate is global
  private fun updateListByDesiredDate() {
    expenseViewModel.getExpensesByDate(desiredDate).observe(this, {
      it?.let {
        recyclerAdapter.submitList(it)
      }
    })
  }

}