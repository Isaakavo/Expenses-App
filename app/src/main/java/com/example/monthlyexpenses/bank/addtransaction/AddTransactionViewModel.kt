package com.example.monthlyexpenses.bank.addtransaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.monthlyexpenses.data.bank.BanksRepository
import com.example.monthlyexpenses.data.bank.Transactions
import com.example.monthlyexpenses.setCurrentDayFormat
import kotlinx.coroutines.launch

class AddTransactionViewModel(private val repository: BanksRepository) : ViewModel() {
	val conceptEditText = MutableLiveData<String>()
	val editTextDate = MutableLiveData<String>()
	val amountEditText = MutableLiveData<String>()
	val transactionsType = MutableLiveData<String>()
	private val _timestamp = MutableLiveData<Long>()

	var isConceptFill = false

	fun setTimestamp(timestamp: Long) {
		_timestamp.value = timestamp
	}

	private val _showDatePicker = MutableLiveData<Boolean>()
	val showDatePicker
		get() = _showDatePicker

	fun displayDatePicker() {
		_showDatePicker.value = true
	}

	fun hideDatePicker() {
		_showDatePicker.value = false
	}

	fun onSendToDatabase() {
		viewModelScope.launch {
			if (isConceptFill) {
				val amount = amountEditText.value?.toFloat()
				val transactions = Transactions(amount, _timestamp.value, transactionsType.value)
				repository.insert(transactions)
			}
		}
	}

	init {
		editTextDate.value = setCurrentDayFormat()
	}
}

class AddTransactionViewModelFactory(private val repository: BanksRepository) :
	ViewModelProvider.Factory {
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		@Suppress("UNCHECKED_CAST")
		if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
			return AddTransactionViewModel(repository) as T
		}
		throw IllegalArgumentException("unknown view model class")
	}

}