package app.pizzabutton.android.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Order(
    val timePlaced: Long,
    val timeETA: Long,
    val transcript: String,
    val store: Store,
) : Parcelable