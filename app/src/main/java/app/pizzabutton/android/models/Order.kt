package app.pizzabutton.android.models

data class Order(
    val timePlaced: Long,
    val timeETA: Long,
    val transcript: String,
    val store: Store,
)