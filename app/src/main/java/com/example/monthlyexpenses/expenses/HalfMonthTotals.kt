package com.example.monthlyexpenses.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentHalfMonthTotalsBinding
import com.example.monthlyexpenses.viewmodel.ExpenseViewModel

//Fragment to show the expenses of the month every fifteen days
class HalfMonthTotals : DialogFragment() {

    private val expenseViewModel: ExpenseViewModel by activityViewModels()
    private lateinit var binding: FragmentHalfMonthTotalsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_half_month_totals,
            container, false)
        binding.monthTotals = expenseViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}