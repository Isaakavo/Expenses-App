package com.example.monthlyexpenses

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
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import data.Expenses
import data.Items
import viewmodel.ExpenseViewModel
import viewmodel.ExpenseViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ExpensesListActivity : AppCompatActivity(), ExpenseListAdapter.OnEditSelectedListener, ExpenseListAdapter.OnClickListener {
  companion object {
    const val flag = "FLAG"
    const val returnFlag = "RETURN_FLAG"
    const val newExpenseActivityRequestCode = 1
    const val editExpenseActivityRequestCode = 2
  }

  private val expenseViewModel: ExpenseViewModel by viewModels {
    ExpenseViewModelFactory((application as ExpensesApplication).repository)
  }
  private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
    val flag = result.data?.getIntExtra(returnFlag, 0)
    if (result.resultCode == Activity.RESULT_OK && flag == newExpenseActivityRequestCode) {
      val intentExpenses = result.data?.extras?.getSerializable(AddNewExpense.EXTRA_EXPENSE) as Expenses
      val itemList = result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS)
      expenseViewModel.insert(intentExpenses, itemList as List<Items>)
    }else if (result.resultCode == Activity.RESULT_OK && flag == editExpenseActivityRequestCode){
      val intentExpenses = result.data?.extras?.getSerializable(AddNewExpense.EXTRA_EXPENSE) as Expenses
      val itemList = result.data!!.extras?.getParcelableArrayList<Items>(AddNewExpense.EXTRA_ITEMS)
      expenseViewModel.updateExpenseAndItems(intentExpenses, itemList as List<Items>)
    }
    else {
      Snackbar.make(this.window.decorView.rootView, "Somethings went wrong!", Snackbar.LENGTH_LONG).show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val totalOfMonth = findViewById<TextView>(R.id.tvTotalMonth)
    val currentMonth = findViewById<TextView>(R.id.tvMonth)

    currentMonth.text = currentDate()

    val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
    val adapter = ExpenseListAdapter(this, this)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)

    expenseViewModel.allExpenses.observe(this, { expenses ->
      // Update the cached copy of the words in the adapter.
      expenses?.let {
        adapter.submitList(it)
        var total = 0F
        for (expense in it) {
          total += expense.total
        }
        totalOfMonth.text = getString(R.string.dollarsingVariable, total.toString())
      }
    })

    val fab = findViewById<FloatingActionButton>(R.id.fab)
    fab.setOnClickListener {
      val intent = Intent(this@ExpensesListActivity, AddNewExpense::class.java)
      intent.putExtra(flag, newExpenseActivityRequestCode)
      adapter.closeMenu()
      startForResult.launch(intent)
    }

    val itemTouchHelper = ItemTouchHelper(touchHelperCallback(adapter))
    itemTouchHelper.attachToRecyclerView(recyclerView)

    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        adapter.closeMenu()
      }
    })
  }

  override fun sendExpenseToEdit(expense: Expenses) {
    val intent = Intent(this@ExpensesListActivity, AddNewExpense::class.java)
    intent.putExtra(flag, editExpenseActivityRequestCode)
    intent.putExtra(AddNewExpense.EXTRA_EXPENSE, expense)
    startForResult.launch(intent)
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  override fun onClickDetected(expense: Expenses) {

    val vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.EFFECT_HEAVY_CLICK))
    }
    expenseViewModel.getItemById(expense.id).observe(this, {
      val detailsFragment = ExpensesDetails.newInstance(expense.concept, expense.date, it)
      detailsFragment.show(supportFragmentManager, "Details")
    })
  }

  private fun touchHelperCallback(adapter: ExpenseListAdapter): ItemTouchHelper.SimpleCallback {
    return object :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

      private val background = ColorDrawable(getColor(R.color.background))
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
        if (dX > 0){
          background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
        } else if (dX < 0) {
          background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        } else {
          background.setBounds(0, 0, 0, 0)
        }
        background.draw(c)
      }
    }
  }

  private fun currentDate(): String {
    val current = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("MMMM y")
    return current.format(formatter)
  }
}