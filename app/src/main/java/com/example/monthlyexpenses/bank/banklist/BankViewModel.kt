package com.example.monthlyexpenses.bank.banklist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.monthlyexpenses.data.bank.BanksRepository

class BankViewModel(repository: BanksRepository) : ViewModel() {

	private val _navigateToAddTransaction = MutableLiveData<Boolean>()
	val navigateToAddTransactions
		get() = _navigateToAddTransaction

	private val _navigateToAddBankentity = MutableLiveData<Boolean>()
	val navigateToAddBankEntity
		get() = _navigateToAddBankentity

	fun onNavigateToAddTransaction() {
		_navigateToAddTransaction.value = true
	}

	fun onNavigatedToTransaction() {
		_navigateToAddTransaction.value = false
	}

	fun onNavigateToAddBankentity() {
		_navigateToAddBankentity.value = true
	}

	fun onNavigatedToBankentity() {
		_navigateToAddBankentity.value = false
	}
}

class BankViewModelFactory(private val repository: BanksRepository) :
	ViewModelProvider.Factory {
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(BankViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return BankViewModel(repository) as T
		}
		throw IllegalArgumentException("Unknown view model class")
	}

}