package com.example.monthlyexpenses.monthtotals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.monthlyexpenses.ExpensesApplication
import com.example.monthlyexpenses.R
import com.example.monthlyexpenses.databinding.FragmentHalfMonthTotalsBinding

//Fragment to show the expenses of the month every fifteen days
class HalfMonthTotals : DialogFragment() {

    private lateinit var expenseViewModel: HalfMonthTotalsViewModel
    private lateinit var binding: FragmentHalfMonthTotalsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val args = HalfMonthTotalsArgs.fromBundle(requireArguments())

        val application = requireNotNull(this.activity).application

        val viewModelFactory =
            HalfMonthTotalsFactory(
                (application as ExpensesApplication).repository,
                args.desiredDate
            )

        expenseViewModel =
            ViewModelProvider(this, viewModelFactory).get(HalfMonthTotalsViewModel::class.java)


        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_half_month_totals,
            container, false
        )
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