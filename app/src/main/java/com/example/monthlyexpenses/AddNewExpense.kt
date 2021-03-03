package com.example.monthlyexpenses

import UI.DatePickerFragment
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import data.Expenses
import data.Items
import viewmodel.ExpenseViewModel
import viewmodel.ExpenseViewModelFactory
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddNewExpense : AppCompatActivity() {


  //UI components
  private lateinit var editTextConcept: EditText
  private lateinit var imageViewAdd: ImageView
  private lateinit var editTextDate: EditText
  private lateinit var buttonAdd: Button

  private var flag = 0
  private var editTextId = 0
  private var timestamp: Long = 0

  private var idExpense = 0L
  private val idItems: MutableList<Long> = mutableListOf()

  private val context: Context = this

  private val itemETList: MutableList<EditText> = mutableListOf()

  private val expenseViewModel: ExpenseViewModel by viewModels {
    ExpenseViewModelFactory((application as ExpensesApplication).repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_new_expense)
    editTextConcept = findViewById(R.id.etConcept)
    imageViewAdd = findViewById(R.id.addNewComment)
    editTextDate = findViewById(R.id.etDate)
    buttonAdd = findViewById(R.id.okbutton)

    editTextDate.setText(setFormattedDate())
    buttonAdd.setOnClickListener(onClickListener)
    editTextDate.setOnClickListener(onClickListener)
    imageViewAdd.setOnClickListener(onClickListener)

    editTextConcept.requestFocus()

    flag = intent.getIntExtra(ExpensesListActivity.flag, 0)
    if (flag == ExpensesListActivity.editExpenseActivityRequestCode) {
      val expenseToEdit = intent.getSerializableExtra(EXTRA_EXPENSE) as Expenses
      timestamp = expenseToEdit.date
      idExpense = expenseToEdit.id
      expenseViewModel.getItemById(expenseToEdit.id).observe(this, { items ->
        editTextConcept.setText(expenseToEdit.concept)
        val lastChar = editTextConcept.text.length
        editTextConcept.setSelection(lastChar)
        editTextDate.setText(DateFormat.getDateInstance().format(expenseToEdit.date))
        items?.let {
          for (item in it) {
            addNewEditText(item.item, item.price)
            idItems.add(item.id)
          }
        }
      })
      buttonAdd.text = getString(R.string.update_button)
    } else if (flag == ExpensesListActivity.newExpenseActivityRequestCode) {
      addNewEditText("", "")
    }
  }

  private fun sendExpenseToAdd() {
    val replyIntent = Intent()

    if (editTextIsNotEmpty()) {
      val itemsArray = getItemComment()
      val priceArray = getItemPrice()
      val itemList: MutableList<Items> = mutableListOf()
      val expense = Expenses(editTextConcept.text.toString(), timestamp, "expense", getTotals())
      expense.id = idExpense
      for (i in 0 until itemsArray.size){
        val item = Items(itemsArray[i], priceArray[i])
        itemList.add(item)
      }
      for (i in 0 until idItems.size){
        if (flag == ExpensesListActivity.editExpenseActivityRequestCode){
          itemList[i].id = idItems[i]
          itemList[i].expenseId = idExpense
        }
      }
      replyIntent.putExtra(EXTRA_EXPENSE, expense)
      replyIntent.putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(itemList))

      when(intent.getIntExtra(ExpensesListActivity.flag, 0)){
        ExpensesListActivity.newExpenseActivityRequestCode -> replyIntent.
        putExtra(ExpensesListActivity.returnFlag, ExpensesListActivity.newExpenseActivityRequestCode)
        ExpensesListActivity.editExpenseActivityRequestCode -> replyIntent.
        putExtra(ExpensesListActivity.returnFlag, ExpensesListActivity.editExpenseActivityRequestCode)
      }
      setResult(Activity.RESULT_OK, replyIntent)
      finish()
    } else {
      setResult(Activity.RESULT_CANCELED, replyIntent)
    }
  }

  private fun addNewEditText(editTextItem: String, editTextPrice: String) {

    var lastChar: Int

    val parent = LinearLayout(context)
    parent.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT)
    parent.orientation = LinearLayout.HORIZONTAL

    val itemEditText = EditText(context)
    val p: LinearLayout.LayoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    itemEditText.layoutParams = p
    itemEditText.hint = "Item"
    itemEditText.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS or InputType.TYPE_CLASS_TEXT
    itemEditText.width = resources.getDimension(R.dimen.etComment).toInt()
    //itemEditText.requestFocus()
    itemEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
    itemEditText.setText(editTextItem)
    lastChar = itemEditText.text.length
    itemEditText.setSelection(lastChar)
    itemEditText.id = editTextId
    editTextId++

    val dollarSign = TextView(context)
    val param = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    param.setMargins(resources.getDimension(R.dimen.dollarmargin).toInt(), 0, 0, 0)
    dollarSign.layoutParams = param
    dollarSign.textSize = 20F
    dollarSign.text = resources.getString(R.string.dollarsign)
    dollarSign.typeface = Typeface.DEFAULT_BOLD

    val totalET = EditText(context)
    val par = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    totalET.setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
    totalET.layoutParams = par
    totalET.width = resources.getDimension(R.dimen.etcommentTotal).toInt()
    totalET.imeOptions = EditorInfo.IME_ACTION_NEXT
    totalET.setText(editTextPrice)
    lastChar = totalET.text.length
    totalET.setSelection(lastChar)
    parent.addView(itemEditText)
    parent.addView(dollarSign)
    parent.addView(totalET)

    if (editTextId == 5) {
      val scroll = findViewById<ScrollView>(R.id.scrollviewLayout)
      scroll?.layoutParams?.height = resources.getDimension(R.dimen.ScrollHeight).toInt()
    }

    val finalParent = findViewById<LinearLayout>(R.id.commentLayout)
    finalParent?.addView(parent)

    itemETList.add(itemEditText)
    itemETList.add(totalET)
  }

  private fun editTextIsNotEmpty(): Boolean {
    return if (editTextConcept.text.isNotEmpty() && editTextDate.text.isNotEmpty()) {
      true
    } else {
      closeKeyboard()
      if (editTextConcept.text.isEmpty()) editTextConcept.backgroundTintList = context.getColorStateList(R.color.red)
      if (editTextDate.text.isEmpty()) editTextDate.backgroundTintList = context.getColorStateList(R.color.red)
      Snackbar.make(window.decorView.rootView, "All fields must be filled", Snackbar.LENGTH_LONG).show()
      false
    }
  }

  private fun setFormattedDate(): String {
    val calendar = Calendar.getInstance()
    timestamp = calendar.timeInMillis
    return DateFormat.getDateInstance().format(timestamp)
  }

  private fun getItemComment(): ArrayList<String> {
    val resultString: ArrayList<String> = arrayListOf()
    for (e: EditText in itemETList) {
      if (e.inputType == InputType.TYPE_TEXT_FLAG_CAP_WORDS or InputType.TYPE_CLASS_TEXT) {
        resultString.add(e.text.toString())
      }
    }
    return resultString
  }

  private fun getItemPrice(): ArrayList<String> {
    val itemsPrice: ArrayList<String> = arrayListOf()
    for (e: EditText in itemETList) {
      if (e.inputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
        val convertedNumber = e.text.toString().toFloat()
        itemsPrice.add(String.format("%.2f", convertedNumber))
      }
    }
    return itemsPrice
  }

  private fun getTotals(): Float {
    var totalItems = 0F
    for (e: EditText in itemETList) {
      if (e.inputType == InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL) {
        totalItems += e.text.toString().toFloat()
      }
    }
    return totalItems
  }

  private fun showDatePickerDialog() {
    val datePicker = DatePickerFragment { day, month, year -> onDateSelected(day, month, year) }
    datePicker.show(supportFragmentManager, "datePicker")
  }

  private fun onDateSelected(day: Int, month: Int, year: Int) {
    val combinedCal = GregorianCalendar(TimeZone.getTimeZone("GMT-06:00"))
    combinedCal.set(year, month, day)
    val timestamp = combinedCal.timeInMillis
    this.timestamp = timestamp
    val selectedDate = DateFormat.getDateInstance().format(timestamp)
    editTextDate.setText(selectedDate.toString())
  }

  private fun closeKeyboard() {
    val view = this.currentFocus
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
  }

  companion object {
    const val EXTRA_EXPENSE = "com.example.android.expenselistsql.EXPENSE"
    const val EXTRA_ITEMS = "com.example.android.expenselistsql.ITEM"
  }

  private val onClickListener = View.OnClickListener {
    when (it.id) {
      R.id.etDate -> showDatePickerDialog()
      R.id.okbutton -> sendExpenseToAdd()
      R.id.addNewComment -> addNewEditText("", "")
    }
  }
}