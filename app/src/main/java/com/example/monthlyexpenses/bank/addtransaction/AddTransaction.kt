package com.example.monthlyexpenses.bank.addtransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.monthlyexpenses.*
import com.example.monthlyexpenses.databinding.FragmentAddTransactionBinding
import com.example.monthlyexpenses.ui.DatePickerFragment
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.util.*

class AddTransaction : DialogFragment(), AdapterView.OnItemSelectedListener {

	private lateinit var binding: FragmentAddTransactionBinding
	private lateinit var addTransactionViewModel: AddTransactionViewModel

	override fun onStart() {
		super.onStart()
		dialog?.window?.setLayout(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.WRAP_CONTENT
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		val application = requireNotNull(this.activity).application
		val viewModelFactory =
			AddTransactionViewModelFactory((application as ExpensesApplication).banksRepository)
		addTransactionViewModel =
			ViewModelProvider(this, viewModelFactory).get(AddTransactionViewModel::class.java)
		binding = DataBindingUtil.inflate(
			inflater,
			R.layout.fragment_add_transaction,
			container,
			false
		)
		binding.viewModel = addTransactionViewModel
		binding.lifecycleOwner = viewLifecycleOwner

		bindSpinner()

		addTransactionViewModel.conceptEditText.observe(viewLifecycleOwner, { concept ->
			if (concept.isNullOrEmpty()) {
				binding.conceptTransaction.backgroundTintList =
					context?.getColorStateList(R.color.red)
				Snackbar.make(
					requireView(),
					getString(R.string.You_must_set_a_concept),
					Snackbar.LENGTH_SHORT
				).show()
			} else {
				addTransactionViewModel.isConceptFill = true
			}
		})

		addTransactionViewModel.showDatePicker.observe(viewLifecycleOwner, { show ->
			if (show) {
				showDatePickerDialog()
				addTransactionViewModel.hideDatePicker()
			}
		})

		return binding.root
	}

	private fun bindSpinner() {
		val arrayAdapter = ArrayAdapter(
			requireContext(),
			R.layout.support_simple_spinner_dropdown_item,
			getTransactionsTaypes()
		)
		arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
		binding.typeTransactionSpinner.adapter = arrayAdapter
		binding.typeTransactionSpinner.onItemSelectedListener = this
	}

	private fun showDatePickerDialog() {
		val datePicker = DatePickerFragment(Calendar.getInstance()) { day, month, year ->
			onDateSelected(
				day,
				month,
				year
			)
		}
		datePicker.show(childFragmentManager, "datePickerTransaction")
	}

	private fun onDateSelected(day: Int, month: Int, year: Int) {
		val combinedCal = GregorianCalendar(TimeZone.getTimeZone("GMT-06:00"))
		combinedCal.set(year, month, day)
		val selectedDate = DateFormat.getDateInstance().format(combinedCal.timeInMillis)
		addTransactionViewModel.editTextDate.value = selectedDate
		addTransactionViewModel.setTimestamp(combinedCal.timeInMillis)
	}

	override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
		val selectedItem = parent?.getItemAtPosition(position) as String
		when (selectedItem) {
			ENTRY -> {
				binding.typeTransactionImage.setImageResource(R.drawable.ic_baseline_arrow_upward_24)
				binding.typeTransactionImage.setColorFilter(
					ContextCompat.getColor(
						requireContext(),
						R.color.editBackground
					)
				)
				addTransactionViewModel.transactionsType.value = selectedItem
			}
			EXIT -> {
				binding.typeTransactionImage.setImageResource(R.drawable.ic_baseline_arrow_downward_24)
				binding.typeTransactionImage.setColorFilter(
					ContextCompat.getColor(
						requireContext(),
						R.color.red
					)
				)
				addTransactionViewModel.transactionsType.value = selectedItem
			}
		}
	}

	override fun onNothingSelected(parent: AdapterView<*>?) {
		TODO("Not yet implemented")
	}
}