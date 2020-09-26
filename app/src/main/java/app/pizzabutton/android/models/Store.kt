package app.pizzabutton.android.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Store(
    val name: String,
    val address: String,
    val phoneNumber: String,
) : Parcelable