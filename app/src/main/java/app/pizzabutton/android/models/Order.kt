package app.pizzabutton.android.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Order(
    var timePlaced: Long = 0L,
    var timeETA: Long = 0L,
    var transcript: String = "",
    var store: Store = Store(),
) : Parcelable