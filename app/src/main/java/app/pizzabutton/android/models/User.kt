package app.pizzabutton.android.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val defaultPizza: String,
    val orderHistory: List<Order>,
) : Parcelable