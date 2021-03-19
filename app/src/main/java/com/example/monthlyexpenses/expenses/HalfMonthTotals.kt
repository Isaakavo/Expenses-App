package com.example.monthlyexpenses.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.monthlyexpenses.R

private const val FIRST_HALF = "param1"
private const val SECOND_HALF = "param2"
private const val TITLE = "param3"
//Fragment to show the expenses of the month every fifteen days
class HalfMonthTotals : DialogFragment() {
    private var firstHalfValue: String? = null
    private var secondHalfValue: String? = null
    private var titleString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstHalfValue = it.getString(FIRST_HALF)
            secondHalfValue = it.getString(SECOND_HALF)
            titleString = it.getString(TITLE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firstHalf = view.findViewById<TextView>(R.id.firstHalf)
        val secondHalf = view.findViewById<TextView>(R.id.secondHalf)
        val title = view.findViewById<TextView>(R.id.halfTitle)

        firstHalf.text = getString(R.string.dollarsingVariable, firstHalfValue)
        secondHalf.text = getString(R.string.dollarsingVariable, secondHalfValue)
        title.text = titleString
    }

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
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_half_month_totals, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String, param3: String) =
            HalfMonthTotals().apply {
                arguments = Bundle().apply {
                    putString(FIRST_HALF, param1)
                    putString(SECOND_HALF, param2)
                    putString(TITLE, param3)
                }
                val fragment = HalfMonthTotals()
                fragment.arguments = arguments
                return fragment
            }
    }
}