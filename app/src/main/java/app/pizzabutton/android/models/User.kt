package app.pizzabutton.android.models

data class User(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val defaultPizza: String,
    val orderHistory: List<Order>,
)