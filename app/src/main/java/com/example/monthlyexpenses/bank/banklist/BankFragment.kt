package com.example.monthlyexpenses.bank.banklist

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.monthlyexpenses.ExpensesApplication
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentBankBinding
import com.example.monthlyexpenses.getSpinnerMonths
import timber.log.Timber

class BankFragment : Fragment(), AdapterView.OnItemSelectedListener {

	private lateinit var binding: FragmentBankBinding
	private lateinit var bankViewModel: BankViewModel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {

		val application = requireNotNull(this.activity).application
		val viewModelFactory =
			BankViewModelFactory((application as ExpensesApplication).banksRepository)
		bankViewModel = ViewModelProvider(this, viewModelFactory).get(BankViewModel::class.java)
		binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bank, container, false)
		binding.viewModel = bankViewModel
		binding.lifecycleOwner = viewLifecycleOwner
		bindViews()

		bankViewModel.navigateToAddTransactions.observe(viewLifecycleOwner, { navigate ->
			Timber.d("$navigate")
			if (navigate) {
				Timber.d("Navigated")
				view?.findNavController()?.navigate(
					BankFragmentDirections.actionNavigationBankToAddTransaction()
				)
				bankViewModel.onNavigatedToTransaction()
			}
		})

		bankViewModel.navigateToAddBankEntity.observe(viewLifecycleOwner, { navigate ->
			if (navigate) {
				view?.findNavController()?.navigate(
					BankFragmentDirections.actionNavigationBankToAddBankEntityFragment()
				)
				bankViewModel.onNavigateToAddBankentity()
			}
		})

		return binding.root
	}


	private fun bindViews() {
		val arrayAdapter =
			ArrayAdapter(
				requireContext(),
				android.R.layout.simple_spinner_dropdown_item,
				getSpinnerMonths(this.context)
			)
		arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
		binding.periodSpinner.adapter = arrayAdapter
		binding.periodSpinner.onItemSelectedListener = this
	}

	override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
		val selectedItem = parent?.getItemAtPosition(position) as String
	}

	override fun onNothingSelected(parent: AdapterView<*>?) {
		return
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		//super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.bank_menu, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.add_bank_entity -> bankViewModel.navigateToAddBankEntity
		}
		return super.onOptionsItemSelected(item)
	}
}