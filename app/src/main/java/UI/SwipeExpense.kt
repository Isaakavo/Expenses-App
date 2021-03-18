package UI

import adapter.ExpenseListAdapter
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.monthlyexpenses.R

class SwipeExpense(context: Context?, val adapter: ExpenseListAdapter) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

  private val background = ColorDrawable(getColor(context!!, R.color.background))

  override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
    return false
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    adapter.showMenu(viewHolder.adapterPosition)
  }

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