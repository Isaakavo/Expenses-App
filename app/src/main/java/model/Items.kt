package model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable

//Dataclass that represents items model
@Entity(tableName = "items_table",
    foreignKeys = [ForeignKey(entity = Expenses::class, parentColumns = ["id"],
        childColumns = ["expenseId"], onDelete = ForeignKey.CASCADE)])
@Parcelize
data class Items(
    @ColumnInfo(name = "item") var item: String = "",
    @ColumnInfo(name = "price") var price: String = "",
    @ColumnInfo(name = "expenseId") var expenseId: Long = 0,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Long = 0
) : Serializable, Parcelable