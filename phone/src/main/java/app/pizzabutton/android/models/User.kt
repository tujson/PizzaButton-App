package app.pizzabutton.android.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var id: String = "",
    var name: String = "",
    var address: String = "",
    var phoneNumber: String = "",
    var defaultPizza: String = "",
    var orderHistory: MutableList<Order> = mutableListOf(),
) : Parcelable